#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/indirect/reservoir.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out uvec3 gi_reservoir_1;

void main() {
    setup_frag_data(1);
    if (!frag_is_in_world) discard;

    vec2 uv = ph_reproject_player_pos(frag_player_pos, frag_is_hand, get_taa_jitter()).xy;
    if (clamp(uv, 0, 1) != uv) discard;

    ivec2 prev_texel = ivec2(uv * PH_VIEW_SIZE);

    FragData prev_frag;
    frag_data_load_previous(prev_frag, prev_texel);

    vec3 n = frag_data_geo_normal(prev_frag);
    if (dot(n, frag_geo_normal) < 0.99f) discard;


    float indirect_sample_weight = 0.0f;
    IndirectReservoir indirect_result = indirect_reservoir_empty();
    IndirectReservoir temp_indirect = indirect_reservoir_empty();

    // load temporal sampled reservoir
    if (indirect_reservoir_load_previous(temp_indirect, prev_texel, true)) {
        temp_indirect.total_samples = min(temp_indirect.total_samples, max_indirect_temporal_samples);
        float shift = indirect_sample_compute_shift(temp_indirect.smple, _frag_data, prev_frag, 20.0f);

        if (shift >= 0.0f) {
            indirect_reservoir_merge(
                    indirect_result,
                    temp_indirect,
                    clamp(shift, 0.0f, 3.0f),
                    indirect_sample_weight
            );
        }
    }

    indirect_reservoir_load(temp_indirect, frag_tex_coord);
    indirect_reservoir_merge(indirect_result, temp_indirect, 1.0f, indirect_sample_weight);

    // write resulting reservoir
    indirect_reservoir_finalize_weight(indirect_result, indirect_sample_weight);
    indirect_reservoir_encode(indirect_result, gi_reservoir_0, gi_reservoir_1);
}
