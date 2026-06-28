#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

#include "/photonics/rendering/indirect_lighting.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;

    uint initial_rnd_state = frag_rnd_state;

    vec3 indirect_result = vec3(0.0f);
    vec3 hit_normal;
    vec3 hit_position;

    sample_indirect(
        indirect_result,
        frag_rt_pos,
        frag_geo_normal,
        frag_is_hand ? frag_geo_normal : frag_tex_normal,
        frag_rnd_state,

        hit_position,
        hit_normal
    );

    IndirectReservoir reservoir = indirect_reservoir_empty();

    indirect_sample_set_color(reservoir.smple, indirect_result);
    indirect_sample_set_rnd_state(reservoir.smple, initial_rnd_state);

    indirect_sample_set_visible_normal(reservoir.smple, frag_geo_normal);
    indirect_sample_set_visible_point(reservoir.smple, frag_rt_pos);

    indirect_sample_set_hit_normal(reservoir.smple, hit_normal);
    indirect_sample_set_hit_position(reservoir.smple, hit_position);

    reservoir.weight = ph_luminance(reservoir.smple.color);
    reservoir.total_samples = 1.0f;

    indirect_reservoir_finalize_weight(reservoir, reservoir.weight);
    indirect_reservoir_encode(reservoir, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
}
