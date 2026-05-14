#version 430

/*
    -- OUTPUT VARIABLES --
*/
layout(location = 3) out vec4 lighting_frag_out;
layout(location = 4) out vec4 lighting_variance_frag_out;

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir_di/restir.glsl"

#ifndef PH_RESTIR_COMBINED_GI
#define SKIP_ACCUMULATION light_list_size == 0 || !prepare_frag(0)
#else
#define SKIP_ACCUMULATION !prepare_frag(0)
#endif

void main() {
    if (SKIP_ACCUMULATION) {
        lighting_frag_out = vec4(0f);
        lighting_variance_frag_out = vec4(0f);

        return;
    }

    SampleHistory smple;
    sample_history_load(smple);

    SampleHistory accumulator;

    if (!frag_is_hand) {
        sample_history_reproject(accumulator);
        sample_history_combine_lighting(accumulator, smple);
    } else accumulator = smple;

#if PH_RESTIR_DENOISER_PASSES != 0
    sample_history_combine_moment(accumulator, smple);
    sample_history_compute_variance(accumulator, smple);
#endif

    lighting_frag_out = accumulator.lighting;
    lighting_variance_frag_out = accumulator.variance;
}