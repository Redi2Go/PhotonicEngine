#version 430

/*
    -- INPUT VARIABLES --
*/
in vec4 direction_vert_out;

/*
    -- OUTPUT VARIABLES --
*/
layout(location = 0) out vec4 position_frag_out;
layout(location = 1) out vec4 normal_frag_out;
layout(location = 2) out vec4 reservoir_frag_out;

#include "/photonics/common/header.glsl"
#include "/photonics/restir/restir.glsl"

void main() {
    if (!is_in_world()) return;

    load_fragment_variables(albedo, world_pos, block_normal, normal);
    rt_pos = world_pos - world_offset;
    bad_angle = is_bad_angle(world_pos, block_normal);
    ph_frag_is_hand = ph_is_hand();

    position_frag_out = vec4(world_pos, 1f);
    normal_frag_out = vec4(
        ph_encode_normal(block_normal),
        ph_encode_normal(normal)
    );

#ifdef PH_ENABLE_BLOCKLIGHT
    Reservoir reservoir = reservoir_new();
    reservoir_init(reservoir);

    light_sample_trace_hit(reservoir.light, false);
    reservoir_compute_weight(reservoir);

    Reservoir previous_reservoir = reservoir_new();
    if (reservoir_reproject(previous_reservoir)) {
        Reservoir temporal_reservoir = reservoir_new();

        if (reservoir_is_valid(reservoir)) {
            reservoir_update(
                temporal_reservoir,
                reservoir.light,
                reservoir.light.weight * reservoir.weight * reservoir.samples,
                reservoir.samples
            );
        }

        previous_reservoir.samples = min(20f * reservoir.samples, previous_reservoir.samples);

        reservoir_update(
            temporal_reservoir,
            previous_reservoir.light,
            previous_reservoir.light.weight * previous_reservoir.weight * previous_reservoir.samples,
            previous_reservoir.samples
        );

        reservoir = temporal_reservoir;
    }

    reservoir_compute_weight(reservoir);
    reservoir_frag_out = reservoir_encode(reservoir);
#endif
}