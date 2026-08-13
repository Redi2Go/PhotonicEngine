#include "/photonics/rendering/restir/common.glsl"
#include "/photonics/rendering/restir/indirect/sample.glsl"
#include "/photonics/utility/normal_encoding.glsl"

#define INDIRECT_RESERVOIR_0 0
#define INDIRECT_RESERVOIR_1 1
#define INDIRECT_OUT 2

uniform sampler2D gi_reservoirs0;
uniform usampler2D gi_reservoirs1;

uniform sampler2D prev_gi_reservoirs0;
uniform usampler2D prev_gi_reservoirs1;

const float max_indirect_temporal_samples = 20.0f;
const float max_indirect_reservoir_samples = 20.0f;

struct IndirectReservoir {
    IndirectSample smple;

    float weight;
    float total_samples;
};

IndirectReservoir indirect_reservoir_empty() {
    return IndirectReservoir(
        indirect_sample_empty(),
        0.0f,
        0.0f
    );
}

bool indirect_reservoir_update(
    inout IndirectReservoir reservoir,
    IndirectSample smple,
    float weight,
    float samples
) {
    reservoir.weight += weight;
    reservoir.total_samples += samples;

    float required_rng = weight / reservoir.weight;
    if (ph_rand_next_float(frag_rnd_state) < required_rng) {
        reservoir.smple = smple;
        return true;
    }

    return false;
}

bool indirect_reservoir_merge(
    inout IndirectReservoir result,
    IndirectReservoir other,
    float jacobian,
    inout float sample_weight
) {
    float other_sample_weight = ph_luminance(other.smple.color);

    float other_weight = other_sample_weight * other.weight * other.total_samples * jacobian;
    if (indirect_reservoir_update(result, other.smple, other_weight, other.total_samples)) {
        sample_weight = other_sample_weight;
        return true;
    }

    return false;
}

void indirect_reservoir_clamp_samples(inout IndirectReservoir reservoir) {
    if (reservoir.total_samples <= max_indirect_reservoir_samples) return;

    reservoir.weight *= max_indirect_reservoir_samples / reservoir.total_samples;
    reservoir.total_samples = max_indirect_reservoir_samples;
}

void indirect_reservoir_validate_visiblity(inout IndirectReservoir reservoir, vec3 rt_pos, out float visiblity) {
    visiblity = 1.0f;

    vec3 hit_point = indirect_sample_get_hit_point(reservoir.smple);

    RayIterator ray;
    ray_iter_begin(ray, rt_pos, hit_point - rt_pos);

    while (true) {
        RayResult result = ray_iter_next(ray);

        if (!ray_result_is_hit(result)) {
            if (!reservoir.smple.hit_sky)
                reservoir.weight = MINIMUM_RESERVOIR_WEIGHT;

            return;
        }

        vec3 pos_diff = ray_result_position(result) - hit_point;
        if (dot(pos_diff, pos_diff) < 0.05f) return;

        if (ray_result_is_transparent(result)) {
            ray_iter_skip_block(ray);
            ray_iter_offset_position(ray, ray.direction * 0.03f);

            continue;
        }

        break;
    }

    visiblity = 0.0f;
    reservoir.weight = MINIMUM_RESERVOIR_WEIGHT;
}

void indirect_reservoir_finalize_weight(
    inout IndirectReservoir reservoir,
    float sample_weight
) {
    if (sample_weight <= 0.0f) {
        reservoir.weight = 0.0f;
        return;
    }

    reservoir.weight = (1.0f / sample_weight) * (reservoir.weight / reservoir.total_samples);
}

vec3 indirect_reservoir_get_final_color(inout IndirectReservoir reservoir) {
    return reservoir.smple.color * reservoir.weight;
}

void indirect_reservoir_encode(IndirectReservoir reservoir, out vec4 data0, out uvec3 data1) {
    data0.xyz = reservoir.smple.hit_point;
    data0.w = max(reservoir.weight, MINIMUM_RESERVOIR_WEIGHT);
    if (reservoir.smple.hit_sky) data0.w = -data0.w;

    data1.x = packHalf2x16(reservoir.smple.color.rg);
    data1.y = packHalf2x16(vec2(reservoir.smple.color.b, reservoir.total_samples));
    data1.z = reservoir.smple.packed_hit_normal;
}

void indirect_reservoir_decode(out IndirectReservoir reservoir, vec4 data0, uvec3 data1) {
    reservoir.smple.hit_point = data0.xyz;
    if (data0.w < 0.0f) {
        reservoir.weight = -data0.w;
        reservoir.smple.hit_sky = true;
    } else {
        reservoir.weight = data0.w;
        reservoir.smple.hit_sky = false;
    }

    vec2 unpacked_value = unpackHalf2x16(data1.x);
    reservoir.smple.color.rg = unpacked_value;

    unpacked_value = unpackHalf2x16(data1.y);
    reservoir.smple.color.b = unpacked_value.x;
    reservoir.total_samples = unpacked_value.y;

    reservoir.smple.packed_hit_normal = data1.z;
}

bool indirect_reservoir_is_nan(IndirectReservoir reservoir) {
    return isnan(reservoir.weight) || isnan(reservoir.total_samples);
}

bool indirect_reservoir_load(out IndirectReservoir reservoir, ivec2 tex_coord) {
    indirect_reservoir_decode(
        reservoir,
        texelFetch(gi_reservoirs0, tex_coord, 0),
        texelFetch(gi_reservoirs1, tex_coord, 0).rgb
    );

    return !indirect_reservoir_is_nan(reservoir);
}

bool indirect_reservoir_load_previous(out IndirectReservoir reservoir, ivec2 tex_coord, bool reprojected) {
    indirect_reservoir_decode(
        reservoir,
        texelFetch(prev_gi_reservoirs0, tex_coord, 0),
        texelFetch(prev_gi_reservoirs1, tex_coord, 0).rgb
    );

    if (reprojected) {
        vec3 camera_offset = cameraPosition - previousCameraPosition;
        reservoir.smple.hit_point -= camera_offset;
        reservoir.smple.color *= get_exposure() / get_previous_exposure();
    }

    return !indirect_reservoir_is_nan(reservoir);
}
