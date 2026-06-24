#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

#define USE_FRAG_PLAYER_POS
#define USE_FRAG_GEO_NORMAL

layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;

void main() {
    setup_frag_data(31);
    if (!frag_is_in_world) discard;

    float direct_sample_weight = 0.0f;
    DirectReservoir direct_result = direct_reservoir_empty();
    DirectReservoir temp_direct = direct_reservoir_empty();

    direct_reservoir_load(temp_direct, frag_tex_coord);
    direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);

    const float reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE;
    const int reuse_samples = PH_RESTIR_SPATIAL_REUSE_SAMPLES;

    for (int i = 0; i < reuse_samples; i++) {
        vec2 offset = 2.0 * vec2(ph_rand_next_float(frag_rnd_state), ph_rand_next_float(frag_rnd_state)) - 1.0f;
        ivec2 sample_texel = ivec2(frag_tex_coord + offset * reuse_radius);

        FragData sample_frag;
        frag_data_load(sample_frag, sample_texel);

        vec3 sample_data = frag_data_player_pos(sample_frag) - frag_player_pos;
        if (dot(sample_data, sample_data) >= 0.6f) continue;

        sample_data = frag_data_geo_normal(sample_frag);
        if (dot(sample_data, frag_geo_normal) < 0.99f) continue;
        if (!direct_reservoir_load(temp_direct, sample_texel)) continue;


        direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
    }

    direct_reservoir_clamp_samples(direct_result);

    direct_reservoir_finalize_weight(direct_result, direct_sample_weight);
    direct_reservoir_encode(direct_result, di_reservoir_0);
}
