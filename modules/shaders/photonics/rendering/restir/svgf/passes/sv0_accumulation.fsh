#version 430

#define FRAG_USE_RT_POS
#define FRAG_USE_TEX_NORMAL

#define REPROJECT_PASS

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/frag/fast_data.glsl"
#include "/photonics/rendering/restir/common.glsl"
#include "/photonics/rendering/restir/svgf/common.glsl"
#include "/photonics/rendering/restir/svgf/history.glsl"

uniform sampler2D di_output;

layout(location = SVGF_HISTORY_OUT) out uvec4 svgf_history;

void main() {
    svgf_history = uvec4(0u);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    vec3 diOutput = texelFetch(di_output, frag_tex_coord, 0).rgb;

    SampleHistory temporalHistory = sample_history_empty();
    sample_history_reproject(temporalHistory);

#if PH_RESTIR_DENOISER_PASSES > 0
    temporalHistory.lighting.rgb *= get_exposure() / get_previous_exposure();
#else
    diOutput /= get_exposure();
#endif

    sample_history_add_sample(temporalHistory, diOutput);
    sample_history_encode(temporalHistory, svgf_history);
}
