#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/indirect/reservoir.glsl"

#include "/photonics/modifiers/restir_gi_modifier.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out uvec3 gi_reservoir_1;
layout(location = INDIRECT_OUT) out vec3 gi_output;

void main() {
    setup_frag_data(3);
    if (!frag_is_in_world) discard;

    float indirect_sample_weight = 0.0f;
    IndirectReservoir indirect_result = indirect_reservoir_empty();
    IndirectReservoir reused_reservoir = indirect_reservoir_empty();

    indirect_reservoir_load(reused_reservoir, frag_tex_coord);
    indirect_reservoir_merge(indirect_result, reused_reservoir, 1.0f, indirect_sample_weight);

#if PH_RESTIR_SPATIAL_REUSE_SAMPLES > 0
    if (indirect_reservoir_load_previous(reused_reservoir, frag_tex_coord, false)) {
        indirect_reservoir_validate_visiblity(reused_reservoir, frag_rt_pos);
        indirect_reservoir_merge(indirect_result, reused_reservoir, 1.0f, indirect_sample_weight);
    }
#else
    indirect_reservoir_validate_visiblity(indirect_result, frag_rt_pos);
#endif

    indirect_reservoir_clamp_samples(indirect_result);
    indirect_reservoir_finalize_weight(indirect_result, indirect_sample_weight);


    gi_output = indirect_reservoir_get_final_color(indirect_result);

#ifndef PH_RESTIR_GI_MODIFIER_DISABLED
    modify_restir_gi(gi_output);
#endif

    indirect_reservoir_encode(indirect_result, gi_reservoir_0, gi_reservoir_1);
}
