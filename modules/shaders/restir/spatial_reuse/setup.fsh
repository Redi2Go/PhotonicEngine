#version 430

/*
    -- INPUT VARIABLES --
*/
in vec4 direction_vert_out;

/*
    -- OUTPUT VARIABLES --
*/
layout(location = 5) out float samples_frag_out;

#include "/photonics/common/header.glsl"
#include "/photonics/restir/restir.glsl"

void main() {
    if (!is_in_world() || ph_light_count == 0) return;

    load_fragment_variables(albedo, world_pos, block_normal, normal);
    rt_pos = world_pos - world_offset;
    bad_angle = is_bad_angle(world_pos, block_normal);
    ph_frag_is_hand = ph_is_hand();

    SampleHistory smple;
    sample_history_reproject(smple);

    samples_frag_out = smple.lighting.a;
}