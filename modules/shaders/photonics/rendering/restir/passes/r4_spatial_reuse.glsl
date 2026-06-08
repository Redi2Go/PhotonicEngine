#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

#if defined PH_ENABLE_BLOCKLIGHT
layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;
#endif

#if defined PH_ENABLE_RESTIR_GI
layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;
#endif

void main() {
    setup_frag_data(REUSE_ITERATION * 31);
    if (!frag_is_in_world) discard;

    float samples = texelFetch(restir_lighting_samples, frag_tex_coord, 0).r;
    bool is_low_samples = samples < min(PH_RESTIR_ACCUMULATION_FRAMES - 1, 4);

#if REUSE_ITERATION > 0
    if (!is_low_samples) discard;
#endif

#if defined PH_ENABLE_BLOCKLIGHT
    float direct_sample_weight = 0.0f;
    DirectReservoir direct_result = direct_reservoir_empty();
    DirectReservoir temp_direct = direct_reservoir_empty();

    direct_reservoir_load(temp_direct, frag_tex_coord);
    direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
#endif


#if defined PH_ENABLE_RESTIR_GI
    float indirect_sample_weight = 0.0f;
    IndirectReservoir indirect_result = indirect_reservoir_empty();
    IndirectReservoir temp_indirect = indirect_reservoir_empty();

    indirect_reservoir_load(temp_indirect, frag_tex_coord);
    indirect_reservoir_merge(indirect_result, temp_indirect, 1.0f, indirect_sample_weight);
#endif


    const float ph_spatial_reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE * (REUSE_ITERATION + 1);
    int reuse_samples = is_low_samples ? PH_RESTIR_SPATIAL_REUSE_SAMPLES * 3 : PH_RESTIR_SPATIAL_REUSE_SAMPLES;

    for (int i = 0; i < reuse_samples; i++) {
        vec2 offset = 2.0 * vec2(ph_rand_next_float(frag_rnd_state), ph_rand_next_float(frag_rnd_state)) - 1.0f;
        ivec2 sample_texel = ivec2(frag_tex_coord + offset * ph_spatial_reuse_radius);

        FragData sample_frag;
        frag_data_load(sample_frag, sample_texel);

        vec3 sample_player_pos = frag_data_player_pos(sample_frag);
        vec3 d = sample_player_pos - frag_player_pos;
        if (dot(d, d) >= 2f) continue;

        vec3 sample_geo_normal = frag_data_geo_normal(sample_frag);
        if (dot(sample_geo_normal, frag_geo_normal) < 0.99f) continue;

#if defined PH_ENABLE_BLOCKLIGHT
        if (direct_reservoir_load(temp_direct, sample_texel))
            direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
#endif

#if defined PH_ENABLE_RESTIR_GI
        if (indirect_reservoir_load(temp_indirect, sample_texel)) {
            temp_indirect.total_samples = 1.0f;

            indirect_reservoir_merge(
                indirect_result,
                temp_indirect,
                1.0f,
                indirect_sample_weight
            );
        }
#endif
    }

#if defined PH_ENABLE_BLOCKLIGHT
    direct_reservoir_clamp_samples(direct_result);

    direct_reservoir_finalize_weight(direct_result, direct_sample_weight);
    direct_reservoir_encode(direct_result, di_reservoir_0);
#endif


#if defined PH_ENABLE_RESTIR_GI
    indirect_reservoir_clamp_samples(indirect_result);

    indirect_reservoir_finalize_weight(indirect_result, indirect_sample_weight);
    indirect_reservoir_encode(indirect_result, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
#endif
}
