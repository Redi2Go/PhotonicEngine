#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = RESTIR_LIGHTING_OUT) out vec4 lighting_frag_out;
layout(location = RESTIR_LIGHTING_VARIANCE_OUT) out vec4 lighting_variance_frag_out;

void main() {
    lighting_frag_out = vec4(0.0f);
    lighting_variance_frag_out = vec4(0.0f);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    SampleHistory smple;
    sample_history_load(smple);

    SampleHistory accumulator;

    sample_history_reproject(accumulator);
    sample_history_combine_lighting(accumulator, smple);

#if PH_RESTIR_DENOISER_PASSES != 0
    sample_history_combine_moment(accumulator, smple);
    sample_history_compute_variance(accumulator, smple);
#endif

    lighting_frag_out = accumulator.lighting;
    lighting_variance_frag_out = accumulator.variance;
}
