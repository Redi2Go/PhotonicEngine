#version 430

#define USE_FRAG_RT_POS
#define USE_FRAG_GEO_NORMAL
#define USE_FRAG_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/indirect/reservoir.glsl"
#include "/photonics/rendering/restir/neighbor/reservoir.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out uvec3 gi_reservoir_1;

void main() {
    setup_frag_data(2);
    if (!frag_is_in_world) discard;

    uvec4 samples;
    neighbor_load_samples(frag_tex_coord, samples);

    float indirect_sample_weight = 0.0f;
    IndirectReservoir indirect_result = indirect_reservoir_empty();
    IndirectReservoir reuse_indirect = indirect_reservoir_empty();

    for (int i = 0; i < PH_RESTIR_SPATIAL_REUSE_SAMPLES; i++) {
        if (samples[i] != 0) {
            ivec2 sample_texel = neighbor_next_sample(samples[i]);
            if (!indirect_reservoir_load_previous(reuse_indirect, sample_texel, false)) continue;

            FragData sample_frag;
            frag_data_load(sample_frag, sample_texel);

            indirect_reservoir_reuse(
                indirect_result,
                reuse_indirect,
                sample_frag,
                max_indirect_reservoir_samples,
                50.0f,
                indirect_sample_weight
            );
        }
    }

    indirect_reservoir_finalize_weight(indirect_result, indirect_sample_weight);
    indirect_reservoir_encode(indirect_result, gi_reservoir_0, gi_reservoir_1);
}
