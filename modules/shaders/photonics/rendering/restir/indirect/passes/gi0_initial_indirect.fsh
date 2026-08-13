#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/indirect/reservoir.glsl"
#include "/photonics/rendering/indirect_lighting.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out uvec3 gi_reservoir_1;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;

    vec3 indirect_result = vec3(0.0f);
    vec3 hit_normal;
    vec3 hit_position;

    // Needs this for compatability
    uint rnd_state = frag_rnd_state;
    sample_indirect(
            indirect_result,
            frag_rt_pos,
            frag_tex_normal,
            rnd_state,

            hit_position,
            hit_normal
    );

    indirect_result *= get_exposure();

    IndirectReservoir reservoir = indirect_reservoir_empty();
    indirect_sample_set_color(reservoir.smple, indirect_result);
    indirect_sample_set_hit_normal(reservoir.smple, hit_normal);
    indirect_sample_set_hit_point(reservoir.smple, hit_position, frag_rt_pos, frag_geo_normal, frag_rnd_state);

    reservoir.weight = ph_luminance(reservoir.smple.color * indirect_sample_normal_factor(_frag_data, hit_position));
    reservoir.total_samples = 1.0f;

    indirect_reservoir_finalize_weight(reservoir, reservoir.weight);
    indirect_reservoir_encode(reservoir, gi_reservoir_0, gi_reservoir_1);
}
