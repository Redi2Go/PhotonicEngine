#version 430

//ph_required: uniform sampler2D depthtex0;
//ph_required: uniform float near, far;

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"

layout(location = 0) out uvec4 denoise_out;

void main() {
    setup_frag_data(0);

    SvgfSample smple = svgf_sample_empty();
    if (frag_is_in_world) {
        smple.depth = texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(frag_tex_coord), 0).r;
        smple.packed_normal = frag_is_hand ? _frag_data.data1.y : _frag_data.data1.z;

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

        smple.color = min(min(center.rgb, maxNeighbour), 65504.0);
        smple.age = center.a;

        float variance_sum = 0.0f;
        float weight_sum = 0.0f;

        for (int i = 0; i < 9; i++) {
            ivec2 p = frag_tex_coord + offset[i];

            float variance = texelFetch(restir_lighting_variance, p, 0).z;
            float kernel_weight = kernel[i];

            variance_sum += variance * kernel_weight;
            weight_sum += kernel_weight;
        }

        smple.variance = variance_sum / weight_sum;
    }

    svgf_sample_encode(smple, denoise_out);
}
