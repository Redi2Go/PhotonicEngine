#version 430

//ph_required: uniform sampler2D prev_denoise_result;

//ph_required: uniform int atrous_iteration;
//ph_required: uniform sampler2D depthtex0;
//ph_Required: uniform float near, far;

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"

#include "/photonics/utility/color.glsl"

layout(location = 0) out vec4 denoise_out;

vec3 ph_get_normal_for_denoise(ivec2 texel) {
    FragData frag;
    frag_data_load(frag, texel);

    return frag_is_hand ? frag_data_geo_normal(frag) : frag_data_tex_normal(frag);
}

void main() {
    denoise_out = vec4(0.0f, 0.0f, 0.0f, 1.0f);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    denoise_out = texelFetch(prev_denoise_result, frag_tex_coord, 0);
    if (!frag_is_hand && atrous_iteration >= PH_RESTIR_DENOISER_PASSES) return;

    int step_width = 1 << atrous_iteration;
    float depth = texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(frag_tex_coord), 0).r;

    // Center fetches
    #define C0 denoise_out.rgb
    #define V0 denoise_out.a

    float L0 = ph_luminance(C0);
    vec3  N0 = frag_is_hand ? frag_geo_normal : frag_tex_normal;
    float D0 = svgf_linearize_depth(depth);


    // 2) Bilateral‐style filter with adaptive color weight
    const float phi_depth = 0.5f;
    float phi_luminance = 6.0f * sqrt(max(0.0f, V0)) + 1e-10;

    vec3 C_sum = vec3(0.0f);
    float W_sum = 0.0f;
    float V_sum = 0.0f;

    for (int i = 0; i < 9; ++i) {
        ivec2 p = frag_tex_coord + step_width * offset[i];

        vec4 sample_data = texelFetch(prev_denoise_result, p, 0);
        #define Ci sample_data.rgb
        #define Vi sample_data.a

        float Li = ph_luminance(Ci);
        vec3  Ni = ph_get_normal_for_denoise(p);
        float Di = svgf_linearize_depth(texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(p), 0).x);
        const float k = kernel[i];

        // Color (luminance) weight
        float wC = svgf_luma_edge_stopping_weight(L0, Li, phi_luminance);

        // Normal weight
        float wN = svgf_normal_edge_stopping_weight(N0, Ni);

        // Position weight
        float wP = svgf_depth_edge_stopping_weight(D0, Di, phi_depth);

        float w = any(isnan(Ci)) ? 0.0f : wC * wN * wP * k;
        W_sum += w;
        C_sum += Ci.xyz * w;
        V_sum += Vi * w * w;
    }

    W_sum = max(0.0001f, W_sum);
    V_sum = max(0.0001f, V_sum);

    denoise_out.rgb = C_sum / W_sum;
    denoise_out.a = max(V_sum / (W_sum * W_sum), 0.0f);
}
