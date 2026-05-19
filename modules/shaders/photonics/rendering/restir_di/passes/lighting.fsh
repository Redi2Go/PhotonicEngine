#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/indirect_lighting.glsl"
#include "/photonics/rendering/restir_di/restir.glsl"
#include "/photonics/modifiers/restir_gi_modifier.glsl"

layout(location = 2) out vec4 reservoir_out;
layout(location = 3) out vec3 lighting_out;

#ifdef PH_RESTIR_SOFT_SHADOWS
#define DO_JITTER true
#else
#define DO_JITTER false
#endif

void main() {
    if (!prepare_frag(0) || light_list_size == 0) return;

    if (light_list_size == 0) {
        lighting_out = vec3(0.0f);
        return;
    }

    Reservoir reservoir = reservoir_new();
    reservoir_decode(
        reservoir,
        texelFetch(restir_reservoirs, frag_tex_coord, 0),
        frag_rt_pos,
        false
    );

    if (reservoir_is_valid(reservoir)) {
        vec3 tint_color;
        float light_transmittance;
        light_sample_trace_hit(reservoir.light, tint_color, light_transmittance, DO_JITTER);

        reservoir_compute_weight(reservoir);
        lighting_out = vec3(reservoir.light.color * tint_color * light_transmittance * reservoir.weight);
    } else {
        lighting_out = vec3(0.0f);
    }

#ifdef PH_RESTIR_COMBINED_GI
    vec3 indirect_color;
    sample_indirect(indirect_color, frag_rt_pos, frag_geo_normal, frag_rnd_state);

#ifndef PH_RESTIR_GI_MODIFIER_DISABLED
    modify_restir_gi(indirect_color);
#endif

    lighting_out+= indirect_color;
#endif

    reservoir_out = reservoir_encode(reservoir);
}