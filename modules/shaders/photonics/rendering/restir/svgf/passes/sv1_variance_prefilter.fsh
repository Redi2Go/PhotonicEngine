#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/common.glsl"
#include "/photonics/rendering/restir/svgf/common.glsl"
#include "/photonics/rendering/restir/svgf/history.glsl"

layout(location = SVGF_DENOISE_OUT) out uvec4 denoise_out;

void main() {
    setup_frag_data(0);

    SvgfSample smple = svgf_sample_empty();
    if (frag_is_in_world) {
        smple.depth = load_depth();
        smple.packed_normal = frag_is_hand ? _frag_data.data1.y : _frag_data.data1.z;
        smple.is_hand = frag_is_hand;

        vec4 center = vec4(0.0f);
        vec3 maxNeighbour = vec3(0.0f);

        float variance_sum = 0.0f;
        float weight_sum = 0.0f;

        for (int i = 0; i < 9; i++) {
            ivec2 p = frag_tex_coord + offset[i];

            SampleHistory history;
            sample_history_load(history, p);

            if (i == SVGF_CENTER_INDEX) {
                center = history.lighting;
            } else {
                maxNeighbour = max(maxNeighbour, history.lighting.rgb);
            }

            float kernel_weight = kernel[i];
            variance_sum += history.variance.z * kernel_weight;
            weight_sum += kernel_weight;
        }

        smple.color = min(min(center.rgb, maxNeighbour), 65504.0);
        smple.variance = variance_sum / weight_sum;

        smple.age = center.a;
    }

    svgf_sample_encode(smple, denoise_out);
}
