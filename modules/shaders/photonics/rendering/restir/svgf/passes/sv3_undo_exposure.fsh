#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/common.glsl"
#include "/photonics/rendering/restir/svgf/common.glsl"

layout(location = 0) out uvec4 denoise_out;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;

    SvgfSample denoised_result = svgf_sample_empty();
    svgf_sample_load(denoised_result, frag_tex_coord);

    denoise_out = uvec4(floatBitsToUint(denoised_result.color / get_exposure()), 0u);
}
