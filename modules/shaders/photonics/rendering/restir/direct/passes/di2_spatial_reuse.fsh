#version 430

#define USE_FRAG_RT_POS
#define USE_FRAG_GEO_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/direct/reservoir.glsl"
#include "/photonics/rendering/restir/neighbor/reservoir.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec3 di_reservoir_0;

void main() {
    setup_frag_data(2);
    if (!frag_is_in_world) discard;

    uvec4 samples;
    neighbor_load_samples(frag_tex_coord, samples);

    float direct_sample_weight = 0.0f;
    DirectReservoir direct_result = direct_reservoir_empty();
    DirectReservoir temp_direct = direct_reservoir_empty();

    for (int i = 0; i < PH_RESTIR_SPATIAL_REUSE_SAMPLES; i++) {
        if (samples[i] != 0) {
            ivec2 sample_texel = neighbor_next_sample(samples[i]);

            if (direct_reservoir_load_previous(temp_direct, sample_texel, false)) {
                direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);
            }
        }
    }

    direct_reservoir_finalize_weight(direct_result, direct_sample_weight);
    direct_reservoir_encode(direct_result, di_reservoir_0);
}
