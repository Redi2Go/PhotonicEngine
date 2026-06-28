#include "/photonics/rendering/restir/direct/sample.glsl"

#define DIRECT_RESERVOIR_0 2

//ph_required: uniform sampler2D restir_direct_reservoirs0;
//ph_required: uniform sampler2D prev_restir_direct_reservoirs0;

const float max_direct_temporal_samples = 20.0f * PH_RESTIR_INITIAL_SAMPLES;
const float max_direct_reservoir_samples = 128.0f;

struct DirectReservoir {
    DirectSample smple;

    float weight;
    float total_samples;
};

DirectReservoir direct_reservoir_empty() {
    return DirectReservoir(
        direct_sample_empty(),
        0.0f,
        0.0f
    );
}

bool direct_reservoir_is_empty(DirectReservoir reservoir) {
    return direct_sample_is_empty(reservoir.smple);
}

bool direct_reservoir_update(
    inout DirectReservoir reservoir,
    DirectSample smple,
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

bool direct_reservoir_merge(
    inout DirectReservoir result,
    DirectReservoir other,
    inout float sample_weight
) {
    float other_sample_weight = direct_sample_get_weight(
        other.smple,
        frag_rt_pos,
        frag_geo_normal,
        frag_is_hand ? frag_geo_normal : frag_tex_normal
    );

    float other_weight = other_sample_weight * other.weight * other.total_samples;
    if (direct_reservoir_update(result, other.smple, other_weight, other.total_samples)) {
        sample_weight = other_sample_weight;
        return true;
    }

    return false;
}

void direct_reservoir_clamp_samples(inout DirectReservoir reservoir) {
    if (reservoir.total_samples <= max_direct_reservoir_samples) return;

    reservoir.weight *= max_direct_reservoir_samples / reservoir.total_samples;
    reservoir.total_samples = max_direct_reservoir_samples;
}

void direct_reservoir_validate_visiblity(inout DirectReservoir reservoir, vec3 sample_pos) {
    if (direct_sample_is_empty(reservoir.smple)) return;

    Light light = direct_sample_get_light(reservoir.smple);

    vec3 to_light = light.position - sample_pos;

    vec3 unused0;
    float unused1;

    if (!trace_light_vis(sample_pos, to_light, light.position, 40, unused0, unused1))
        reservoir.weight = 0.0f;
}

void direct_reservoir_finalize_weight(
    inout DirectReservoir reservoir,
    float sample_weight
) {
    if (sample_weight <= 0.0f) {
        reservoir.weight = 0.0f;
        return;
    }

    reservoir.weight = (1.0f / sample_weight) * (reservoir.weight / reservoir.total_samples);
}

vec3 direct_reservoir_get_final_color(
    inout DirectReservoir reservoir,
    vec3 sample_pos,
    vec3 geo_normal,
    vec3 tex_normal
) {
    if (direct_sample_is_empty(reservoir.smple))
        return vec3(0.0f);

    Light light = direct_sample_get_light(reservoir.smple);

#ifdef PH_RESTIR_SOFT_SHADOWS
    vec3 trace_position = light.position;
    ph_rand_sample_position(frag_rnd_state, trace_position, sample_pos);
#else
#define trace_position light.position
#endif

    vec3 to_light = trace_position - sample_pos;

    vec3 tint_color;
    float light_transmittance;

    if (!trace_light_vis(sample_pos, to_light, trace_position, 40, tint_color, light_transmittance)) {
        reservoir.weight = 0.0f;
        return vec3(0.0f);
    }

    vec3 sampled_color = direct_sample_get_color(reservoir.smple, light, sample_pos, geo_normal, tex_normal);
    vec3 final_color = sampled_color * tint_color.rgb * light_transmittance;

    return final_color * reservoir.weight;
}

void direct_reservoir_encode(DirectReservoir reservoir, out vec3 data0) {
    data0[0] = float(reservoir.smple.light_index);
    data0[1] = max(reservoir.weight, MINIMUM_RESERVOIR_WEIGHT);
    data0[2] = reservoir.total_samples;
}

void direct_reservoir_decode(out DirectReservoir reservoir, vec3 data0) {
    reservoir.smple.light_index = int(data0[0]);
    reservoir.weight            = data0[1];
    reservoir.total_samples     = data0[2];
}

bool direct_reservoir_is_nan(DirectReservoir reservoir) {
    return isnan(reservoir.weight) || isnan(reservoir.total_samples);
}

bool direct_reservoir_load(out DirectReservoir reservoir, ivec2 tex_coord) {
    direct_reservoir_decode(
        reservoir,
        texelFetch(restir_direct_reservoirs0, tex_coord, 0).rgb
    );

    return !direct_reservoir_is_nan(reservoir);
}

bool direct_reservoir_load_previous(out DirectReservoir reservoir, ivec2 tex_coord) {
    direct_reservoir_decode(
        reservoir,
        texelFetch(prev_restir_direct_reservoirs0, tex_coord, 0).rgb
    );

    return !direct_reservoir_is_nan(reservoir) && direct_sample_reproject(reservoir.smple);
}
