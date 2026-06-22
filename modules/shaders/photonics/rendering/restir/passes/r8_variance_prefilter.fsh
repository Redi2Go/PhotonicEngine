#version 430

//ph_required: uniform float near, far;

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"

layout(location = 0) out vec4 denoise_out;

void main() {
    denoise_out.rgb = texelFetch(restir_lighting, frag_tex_coord, 0).rgb;
    denoise_out.a = 5.0f;

    setup_frag_data(0);
    if (!frag_is_in_world || frag_is_hand) return;

    denoise_out.a = 0.0f;
    float weight_sum = 0.0f;

    for (int i = 0; i < 9; i++) {
        ivec2 p = frag_tex_coord + offset[i];

        float variance = texelFetch(restir_lighting_variance, frag_tex_coord, 0).r;
        float kernel_weight = kernel[i];

        denoise_out.a += variance * kernel_weight;
        weight_sum += kernel_weight;
    }

    denoise_out.a /= weight_sum;
}
