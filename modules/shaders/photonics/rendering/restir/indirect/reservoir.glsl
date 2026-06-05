#include "/photonics/rendering/restir/indirect/sample.glsl"
#include "/photonics/utility/normal_encoding.glsl"

//TODO Rename restir combined gi
#if defined PH_ENABLE_GI && defined PH_RESTIR_COMBINED_GI
#define PH_ENABLE_RESTIR_GI
#endif

#if defined PH_ENABLE_BLOCKLIGHT
#define INDIRECT_RESERVOIR_0 4
#define INDIRECT_RESERVOIR_1 5
#define INDIRECT_RESERVOIR_2 6
#else
#define INDIRECT_RESERVOIR_0 3
#define INDIRECT_RESERVOIR_1 4
#define INDIRECT_RESERVOIR_2 5
#endif

//ph_required: uniform sampler2D restir_indirect_reservoirs0;
//ph_required: uniform sampler2D restir_indirect_reservoirs1;
//ph_required: uniform sampler2D restir_indirect_reservoirs2;

//ph_required: uniform sampler2D prev_restir_indirect_reservoirs0;
//ph_required: uniform sampler2D prev_restir_indirect_reservoirs1;
//ph_required: uniform sampler2D prev_restir_indirect_reservoirs2;

const float max_indirect_temporal_samples = 4.0f;
const float max_indirect_reservoir_samples = 12.0f;

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
    // is causing a crash on nvidia windows(???)
//    vec3 color = indirect_sample_validate_visibility(reservoir.smple, frag_rt_pos);
//    if (all(equal(color, vec3(0.0f)))) {
//        reservoir.smple.color = vec3(0.0f);
//        reservoir.weight = 0.0f;
//
//        return vec3(0.0f);
//    }

//    return color * reservoir.weight;

    return reservoir.smple.color * reservoir.weight;
}

void indirect_reservoir_encode(
    IndirectReservoir reservoir,
    out vec4 data0,
    out vec4 data1,
    out vec4 data2
) {
    data0.xyz = reservoir.smple.hit_position;
    data0.w = reservoir.weight;

    data1.rgb = reservoir.smple.color;
    data1.a = reservoir.total_samples;

    data2.x = uintBitsToFloat(packUnorm2x16(ph_encode_normal(reservoir.smple.visible_normal)));
    data2.y = uintBitsToFloat(packUnorm2x16(ph_encode_normal(reservoir.smple.hit_normal)));


    const uint sign_bit = 1u << 31;
    uint hit_distance = floatBitsToUint(reservoir.smple.hit_distance);

    if (reservoir.smple.traced_sky)
        hit_distance |= sign_bit;

    data2.z = uintBitsToFloat(hit_distance);
    data2.w = uintBitsToFloat(reservoir.smple.rnd_state);
}

void indirect_reservoir_decode(
    out IndirectReservoir reservoir,
    vec4 data0,
    vec4 data1,
    vec4 data2
) {
    reservoir.smple.hit_position = data0.xyz;
    reservoir.weight = data0.w;

    reservoir.smple.color = data1.rgb;
    reservoir.total_samples = data1.a;

    reservoir.smple.visible_normal = ph_decode_normal(data2.xy);

    reservoir.smple.visible_normal = ph_decode_normal(unpackUnorm2x16(floatBitsToUint(data2.x)));
    reservoir.smple.hit_normal = ph_decode_normal(unpackUnorm2x16(floatBitsToUint(data2.y)));

    const uint sign_bit = 1u << 31;
    uint hit_distance = floatBitsToUint(data2.z);

    reservoir.smple.hit_distance = uintBitsToFloat(hit_distance & ~sign_bit);
    reservoir.smple.traced_sky = (hit_distance & sign_bit) != 0u;

    reservoir.smple.rnd_state = floatBitsToUint(data2.w);
}

bool indirect_reservoir_is_nan(IndirectReservoir reservoir) {
    return isnan(reservoir.weight) || isnan(reservoir.total_samples);
}

bool indirect_reservoir_load(out IndirectReservoir reservoir, ivec2 tex_coord) {
    indirect_reservoir_decode(
        reservoir,
        texelFetch(restir_indirect_reservoirs0, tex_coord, 0),
        texelFetch(restir_indirect_reservoirs1, tex_coord, 0),
        texelFetch(restir_indirect_reservoirs2, tex_coord, 0)
    );

    return !indirect_reservoir_is_nan(reservoir);
}

bool indirect_reservoir_load_previous(out IndirectReservoir reservoir, ivec2 tex_coord) {
    indirect_reservoir_decode(
        reservoir,
        texelFetch(prev_restir_indirect_reservoirs0, tex_coord, 0),
        texelFetch(prev_restir_indirect_reservoirs1, tex_coord, 0),
        texelFetch(prev_restir_indirect_reservoirs2, tex_coord, 0)
    );

    vec3 camera_offset = previousCameraPosition - cameraPosition;
    reservoir.smple.hit_position+= camera_offset;

    return !indirect_reservoir_is_nan(reservoir);
}
