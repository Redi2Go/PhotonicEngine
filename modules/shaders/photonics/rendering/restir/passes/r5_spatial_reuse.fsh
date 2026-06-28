#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

#define USE_FRAG_RT_POS
#define USE_FRAG_GEO_NORMAL

#if defined PH_ENABLE_BLOCKLIGHT
layout(location = DIRECT_RESERVOIR_0) out vec3 di_reservoir_0;
#endif

#if defined PH_ENABLE_RESTIR_GI
layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;
#endif

void main() {
    setup_frag_data(31);
    if (!frag_is_in_world) discard;

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

    const float reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE;
    const int reuse_samples = PH_RESTIR_SPATIAL_REUSE_SAMPLES;

    for (int i = 0; i < reuse_samples; i++) {
        vec2 offset = 2.0 * vec2(ph_rand_next_float(frag_rnd_state), ph_rand_next_float(frag_rnd_state)) - 1.0f;
        ivec2 sample_texel = ivec2(frag_tex_coord + offset * reuse_radius);

        FragData sample_frag;
        frag_data_load(sample_frag, sample_texel);

        vec3 sample_data = frag_data_rt_pos(sample_frag) - frag_rt_pos;
        if (dot(sample_data, sample_data) >= 0.6f) continue;

        sample_data = frag_data_geo_normal(sample_frag);
        if (dot(sample_data, frag_geo_normal) < 0.99f) continue;

#if defined PH_ENABLE_BLOCKLIGHT
        if (direct_reservoir_load(temp_direct, sample_texel))
            direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
#endif

#if defined PH_ENABLE_RESTIR_GI
        if (indirect_reservoir_load(temp_indirect, sample_texel)) {
            temp_indirect.total_samples = min(temp_indirect.total_samples, max_indirect_reservoir_samples);

            indirect_reservoir_merge(
                indirect_result,
                temp_indirect,
                min(indirect_sample_compute_jacobian(temp_indirect.smple, frag_rt_pos), 1.0f), // figure out why it dies when not clamped to 1
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
