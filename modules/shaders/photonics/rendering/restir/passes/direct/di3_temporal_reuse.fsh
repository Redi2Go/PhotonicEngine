#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;

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

    float direct_sample_weight = 0.0f;
    DirectReservoir direct_result = direct_reservoir_empty();
    DirectReservoir temp_direct = direct_reservoir_empty();

    // load freshly sampled reservoir
    direct_reservoir_load(temp_direct, frag_tex_coord);
    direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);

    // load temporal sampled reservoir
    if (direct_reservoir_load_previous(temp_direct, prev_texel)) {
        temp_direct.total_samples = min(max_direct_temporal_samples, temp_direct.total_samples);
        direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
    }

    // write resulting reservoir
    direct_reservoir_finalize_weight(direct_result, direct_sample_weight);
    direct_reservoir_encode(direct_result, di_reservoir_0);
}
