#version 430

#define USE_FRAG_RT_POS
#define USE_FRAG_GEO_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"
#include "/photonics/rendering/restir/neighbor/reservoir.glsl"

#if defined PH_ENABLE_BLOCKLIGHT
layout(location = DIRECT_RESERVOIR_0) out vec3 di_reservoir_0;
#endif


#if defined PH_ENABLE_RESTIR_GI
layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;
#endif

void main() {
    setup_frag_data(961);
    if (!frag_is_in_world) discard;

    uvec4 samples;
    neighbor_load_samples(frag_tex_coord, samples);

#if defined PH_ENABLE_BLOCKLIGHT
    float direct_sample_weight = 0.0f;
    DirectReservoir direct_result = direct_reservoir_empty();
    DirectReservoir temp_direct = direct_reservoir_empty();

    direct_reservoir_load_previous(temp_direct, frag_tex_coord, false);
    direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
#endif


#if defined PH_ENABLE_RESTIR_GI
    float indirect_sample_weight = 0.0f;
    IndirectReservoir indirect_result = indirect_reservoir_empty();
    IndirectReservoir sample_indirect = indirect_reservoir_empty();
#endif

    for (int i = 0; i < NEIGHBOR_SAMPLES; i++) {
        ivec2 sample_texel = neighbor_next_sample(samples[i]);

#if defined PH_ENABLE_RESTIR_GI
        FragData sample_frag;
        frag_data_load(sample_frag, sample_texel);
#endif

#if defined PH_ENABLE_BLOCKLIGHT
        if (direct_reservoir_load_previous(temp_direct, sample_texel, false)) {
            direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
        }
#endif


#if defined PH_ENABLE_RESTIR_GI
        if (indirect_reservoir_load_previous(sample_indirect, sample_texel, false)) {
            sample_indirect.total_samples = min(sample_indirect.total_samples, max_indirect_reservoir_samples);

            vec3 hit_point = indirect_sample_get_hit_point(sample_indirect.smple);
            float occlusion_old = indirect_normal_factor(sample_frag, hit_point);
            float occlusion_new = indirect_normal_factor(_frag_data, hit_point);

            float occlusion_factor = occlusion_new / occlusion_old;
            float jacobian_factor = indirect_sample_compute_jacobian(sample_indirect.smple, frag_rt_pos);

            if (occlusion_factor < 1.5f) {
                indirect_reservoir_merge(
                        indirect_result,
                        sample_indirect,
                        clamp(jacobian_factor, 0.0f, 1.0f) * occlusion_factor,
                        indirect_sample_weight
                );
            }
        }
#endif
    }

#if defined PH_ENABLE_BLOCKLIGHT
    direct_reservoir_clamp_samples(direct_result);

    direct_reservoir_finalize_weight(direct_result, direct_sample_weight);
    direct_reservoir_encode(direct_result, di_reservoir_0);
#endif

#if defined PH_ENABLE_RESTIR_GI
    indirect_reservoir_finalize_weight(indirect_result, indirect_sample_weight);
    indirect_reservoir_encode(indirect_result, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
#endif
}
