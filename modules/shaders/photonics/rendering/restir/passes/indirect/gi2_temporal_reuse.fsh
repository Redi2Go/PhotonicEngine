#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;

    vec3 previous_player_pos;
    vec2 uv = ph_reproject_player_pos(frag_player_pos, frag_is_hand, get_taa_jitter(), previous_player_pos).xy;

    if (clamp(uv, 0, 1) != uv) discard;

    ivec2 prev_texel = ivec2(uv * PH_VIEW_SIZE);

    FragData prev_frag;
    frag_data_load_previous(prev_frag, prev_texel);

    if (!frag_is_bad_angle) {
        vec3 projected_player_pos = frag_data_player_pos(prev_frag);
        vec3 d = projected_player_pos - previous_player_pos;
        if (dot(d, d) >= 0.3f) discard;
    }

    vec3 n = frag_data_geo_normal(prev_frag);
    if (dot(n, frag_geo_normal) < 0.99f) discard;

    float indirect_sample_weight = 0.0f;
    IndirectReservoir indirect_result = indirect_reservoir_empty();
    IndirectReservoir temp_indirect = indirect_reservoir_empty();

    indirect_reservoir_load(temp_indirect, frag_tex_coord);
    indirect_reservoir_merge(indirect_result, temp_indirect, 1.0f, indirect_sample_weight);

    // load temporal sampled reservoir
    if (indirect_reservoir_load_previous(temp_indirect, prev_texel)) {
        temp_indirect.total_samples = min(max_indirect_temporal_samples, temp_indirect.total_samples);
        indirect_reservoir_merge(indirect_result, temp_indirect, 1.0f, indirect_sample_weight);
    }

    // write resulting reservoir
    indirect_reservoir_finalize_weight(indirect_result, indirect_sample_weight);
    indirect_reservoir_encode(indirect_result, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
}
