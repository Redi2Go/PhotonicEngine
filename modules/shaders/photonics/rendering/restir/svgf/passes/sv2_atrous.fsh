#version 430

//ph_required: uniform int atrous_iteration;
//ph_Required: uniform float near, far;

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/common.glsl"
#include "/photonics/rendering/restir/svgf/common.glsl"

#include "/photonics/utility/color.glsl"

layout(location = 0) out uvec4 denoise_out;

float get_pass_weight(SvgfSample smple) {
    const float frame_cutoff = PH_RESTIR_ACCUMULATION_FRAMES * 0.33f;
    if (frag_is_hand || atrous_iteration < PH_RESTIR_DENOISER_PASSES) return 1.0f;

    return 1.0f - (smple.age / frame_cutoff);
}

void main() {
    SvgfSample center_sample = svgf_sample_empty();
    svgf_sample_load(center_sample, frag_tex_coord);

    if (center_sample.depth == 1.0f) {
        denoise_out = uvec4(0u);
        return;
    }

    float pass_weight = get_pass_weight(center_sample);
    if (pass_weight > 0.0f) {
        #define C0 center_sample.color
        #define V0 center_sample.variance

        float L0 = ph_luminance(C0);
        vec3  N0 = svgf_sample_get_normal(center_sample);
        float D0 = svgf_linearize_depth(center_sample.depth);

        vec3 C_sum = vec3(0.0f);
        float W_sum = 0.0f;
        float V_sum = 0.0f;

        int step_width = 1 << atrous_iteration;

        const float phi_depth = 0.5f;
        float phi_luminance = 6.0f * sqrt(max(0.0f, V0)) + 1e-10;
        for (int i = 0; i < 9; ++i) {
            ivec2 p = frag_tex_coord + step_width * offset[i];

            SvgfSample sample_data = svgf_sample_empty();
            svgf_sample_load(sample_data, p);

            #define Ci sample_data.color
            #define Vi sample_data.variance

            float Li = ph_luminance(Ci);
            vec3  Ni = svgf_sample_get_normal(sample_data);
            float Di = svgf_linearize_depth(sample_data.depth);
            const float k = kernel[i];

            // Color (luminance) weight
            float wC = frag_is_hand ? 1.0f : svgf_luma_edge_stopping_weight(L0, Li, phi_luminance);

            // Normal weight
            float wN = svgf_normal_edge_stopping_weight(N0, Ni);

            // Position weight
            float wP = svgf_depth_edge_stopping_weight(D0, Di, phi_depth);

            float w = wC * wN * wP * k;
            W_sum += w;
            C_sum += Ci.xyz * w;
            V_sum += Vi * w * w;
        }

        W_sum = max(0.0001f, W_sum);
        V_sum = max(0.0001f, V_sum);

        center_sample.color = mix(center_sample.color, C_sum / W_sum, pass_weight);
        center_sample.variance = max(V_sum / (W_sum * W_sum), 0.0f);
    }

    svgf_sample_encode(center_sample, denoise_out);
}
