#version 430

//ph_required: uniform float near, far;

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"

layout(location = 0) out vec4 denoise_out;

void main() {
    // Firefly rejection
    vec4 center = texelFetch(restir_lighting, frag_tex_coord, 0);
    vec3 maxNeighbour = vec3(0.0f);
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            if (x == 0 && y == 0) continue;

            ivec2 pos = frag_tex_coord + ivec2(x, y);
            vec3 color = texelFetch(restir_lighting, pos, 0).rgb;
            maxNeighbour = max(maxNeighbour, color);
        }
    }

    denoise_out = vec4(min(center.rgb, maxNeighbour), 10.0f);

    setup_frag_data(0);
    if (!frag_is_in_world || frag_is_hand) return;

    denoise_out.a = 0.0f;
    float weight_sum = 0.0f;

    for (int i = 0; i < 9; i++) {
        ivec2 p = frag_tex_coord + offset[i];

        float variance = texelFetch(restir_lighting_variance, p, 0).z;
        float kernel_weight = kernel[i];

        denoise_out.a += variance * kernel_weight;
        weight_sum += kernel_weight;
    }

    denoise_out.a /= weight_sum;
}
