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
uniform sampler2D gi_output;

layout(location = SVGF_HISTORY_OUT) out uvec4 svgf_history;

void main() {
    svgf_history = uvec4(0u);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

#if defined PH_ENABLE_BLOCKLIGHT
    vec3 di_output = texelFetch(di_output, frag_tex_coord, 0).rgb;
#else
    const vec3 di_output = vec3(0.0f);
#endif

#if defined PH_ENABLE_RESTIR_GI
    vec3 gi_output = texelFetch(gi_output, frag_tex_coord, 0).rgb;
#else
    const vec3 gi_output = vec3(0.0f);
#endif

    SampleHistory temporal_history = sample_history_empty();
    sample_history_reproject(temporal_history);

#if PH_RESTIR_DENOISER_PASSES > 0
    temporal_history.lighting.rgb *= get_exposure() / get_previous_exposure();
#endif

    sample_history_add_sample(temporal_history, di_output + gi_output);
    sample_history_encode(temporal_history, svgf_history);
}
