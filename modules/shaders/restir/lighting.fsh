#version 430

/*
    -- INPUT VARIABLES --
*/
in vec4 direction_vert_out;

/*
    -- OUTPUT VARIABLES --
*/
layout(location = 2) out vec4 reservoir_frag_out;
layout(location = 3) out vec4 lighting_frag_out;

#ifdef PH_ENABLE_HANDHELD_LIGHT
layout(location = 6) out vec4 handheld_frag_out;
#endif

#define PH_LIGHTING_PASS
#include "/photonics/common/header.glsl"

vec3 ph_sun_direction = ph_signed_nudge(sun_direction);

#include "/photonics/restir/restir.glsl"
#include "/photonics/common/ph_lighting_common.glsl"
#include "/photonics/modifiers/restir_gi_modifier.glsl"

void main() {
    if (!is_in_world()) {
        lighting_frag_out = vec4(0f);

        #ifdef PH_ENABLE_HANDHELD_LIGHT
        handheld_frag_out = vec4(0f);
        #endif


        return;
    }

    load_fragment_variables(albedo, world_pos, block_normal, normal);
    rt_pos = world_pos - world_offset;
    bad_angle = is_bad_angle(world_pos, block_normal);
    ph_frag_is_hand = ph_is_hand();

    #ifdef PH_ENABLE_HANDHELD_LIGHT
    sample_handheld(handheld_frag_out);
    #endif

#ifdef PH_ENABLE_GI
    RAY_ITERATION_COUNT = 20;

    #ifndef PH_RESTIR_COMBINED_GI
        #define INDIRECT_COLOR vec4(0f)

        // Detect edge
        // TODO: we probably should do fract(2.0f * base_position)
        ivec3 inside = ivec3(lessThan(abs(fract(rt_pos) - 0.5f), vec3(0.48f)));
        bool onEdge = inside.x + inside.y + inside.z <= 1;

        if (!onEdge) sample_indirect();
    #else
        vec4 indirect_color = vec4(ph_sample_indirect_impl(), 0f);

        #ifndef PH_RESTIR_GI_MODIFIER_DISABLED
        modify_restir_gi(indirect_color.xyz);
        #endif

        #define INDIRECT_COLOR indirect_color
    #endif

    RAY_ITERATION_COUNT = 100;
#else
    #define INDIRECT_COLOR vec4(0f)
#endif

    if (ph_light_count == 0) {
        lighting_frag_out = INDIRECT_COLOR;
        return;
    }

    Reservoir reservoir = reservoir_new();
    vec4 frag = texelFetch(radiosity_reservoirs, tex_coord, 0);

#ifdef PH_ENABLE_BLOCKLIGHT
    reservoir_decode(reservoir, frag, rt_pos, false);

    if (reservoir_is_valid(reservoir)) {
        #ifdef PH_RESTIR_SOFT_SHADOWS
        light_sample_trace_hit(reservoir.light, true);
        #else
        light_sample_trace_hit(reservoir.light, false);
        #endif

        reservoir_compute_weight(reservoir);

        reservoir_frag_out = reservoir_encode(reservoir);
        lighting_frag_out = vec4(reservoir.light.color * reservoir.weight * result_tint_color, 1f) + INDIRECT_COLOR;
    } else {
        reservoir_frag_out = reservoir_encode(reservoir);
        lighting_frag_out = INDIRECT_COLOR;
    }
#else
    lighting_frag_out = INDIRECT_COLOR;
#endif
}