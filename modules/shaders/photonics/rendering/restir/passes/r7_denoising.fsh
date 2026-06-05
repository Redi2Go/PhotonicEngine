#version 430

/*
    -- OUTPUT VARIABLES --
*/
layout(location = 0) out vec3 color_out;
layout(location = 1) out float variance_out;

uniform int atrous_iteration;
uniform sampler2D depthtex0;
//uniform float near, far;

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

// 3×3 Gaussian Kernel & Offsets
const float kernel[9] = float[](
    1.0/6., 2.0/3., 1.0/6.,
    2.0/3., 1.0   , 2.0/3.,
    1.0/6., 2.0/3., 1.0/6.
);

const ivec2 offset[9] = ivec2[](
    ivec2(-1, -1), ivec2(0, -1), ivec2(1, -1),
    ivec2(-1,  0), ivec2(0,  0), ivec2(1,  0),
    ivec2(-1,  1), ivec2(0,  1), ivec2(1,  1)
);

// Rec.709 luminance coefficients
const vec3 LUM_COEFF = vec3(0.2126, 0.7152, 0.0722);

float normal_edge_stopping_weight(vec3 center_normal, vec3 sample_normal)
{
    const float power = 128.0f;

    return pow(clamp(dot(center_normal, sample_normal), 0.0f, 1.0f), power);
}

float depth_edge_stopping_weight(float center_depth, float sample_depth, float phi)
{
    return exp(-abs(center_depth - sample_depth) / phi);
}

float luma_edge_stopping_weight(float center_luma, float sample_luma, float phi)
{
    return exp(-abs(center_luma - sample_luma) / phi);
}

float ph_linearize_depth(float d)
{
    return near * far / (far + d * (near - far));
}

#include "/photonics/modifiers/restir_denoiser_depth_fetch_modifier.glsl"

#ifdef PH_RESTIR_DENOISER_DEPTH_FETCH_MODIFIER_DISABLED
#define PH_DEPTH_MODIFIER(p) p
#else
#define PH_DEPTH_MODIFIER(p) modify_denoiser_depth_fetch(p)
#endif

vec3 ph_decode_lighting_normal(vec4 packed_normals) {
    return ph_decode_normal(frag_is_hand ? packed_normals.xy : packed_normals.zw);
}

uniform sampler2D denoise_color;
uniform sampler2D denoise_variance;

uniform sampler2D prev_denoise_color;
uniform sampler2D prev_denoise_variance;

void main() {
    if (!prepare_frag(0)) {
        color_out = vec3(0.0f);
        variance_out = 1.0f;

        return;
    }

    if (!frag_is_hand && atrous_iteration >= PH_RESTIR_DENOISER_PASSES) {
        color_out = texelFetch(prev_denoise_color, frag_tex_coord, 0).rgb;
        variance_out = texelFetch(prev_denoise_variance, frag_tex_coord, 0).x;

        return;
    } else if (atrous_iteration == -1) {
        vec3 color = texelFetch(restir_lighting, frag_tex_coord, 0).rgb;
        if (any(isnan(color))) color = vec3(0.0f); // TODO: Find cause of this nan

        color_out = color;
        variance_out = texelFetch(restir_lighting_variance, frag_tex_coord, 0).z;

        return;
    }

    int step_width = 1 << atrous_iteration;
    float depth = texelFetch(depthtex0, PH_DEPTH_MODIFIER(frag_tex_coord), 0).r;

    // Center fetches
    vec3  C0 = texelFetch(prev_denoise_color, frag_tex_coord, 0).rgb;
    float L0 = dot(C0, LUM_COEFF);
    vec3  N0 = ph_decode_lighting_normal(texelFetch(restir_normal_history, frag_tex_coord, 0));
    float D0 = ph_linearize_depth(depth);

    const float phi_depth = 0.5f;

    float V0 = 0f;
    if (!frag_is_hand) {
        float sumw = 0.0f;
        for (int i = 0; i < 9; ++i) {
            ivec2 p = frag_tex_coord + offset[i];

            float Vi = texelFetch(prev_denoise_variance, p, 0).x;
            vec3  Ni = ph_decode_normal(texelFetch(restir_normal_history, p, 0).xy);
            float Di = ph_linearize_depth(texelFetch(depthtex0, PH_DEPTH_MODIFIER(p), 0).x);

            // Normal weight
            float wN = normal_edge_stopping_weight(N0, Ni);

            // Position weight
            float wP = depth_edge_stopping_weight(D0, Di, phi_depth);

            float k   = kernel[i];
            float w = k * wN * wP;

            V0 += Vi * w;
            sumw += w;
        }
        V0 /= sumw;
    } else V0 = 5f;

    float phi_luminance = 6.0f * sqrt(max(0.0f, V0)) + 1e-10;

    // 2) Bilateral‐style filter with adaptive color weight
    vec3 C_sum = vec3(0.0f);
    float W_sum = 0.0f;
    float V_sum = 0.0f;

    for (int i = 0; i < 9; ++i) {
        ivec2 p  = frag_tex_coord + step_width * offset[i];
        vec3  Ci = texelFetch(prev_denoise_color, p, 0).rgb;
        float Vi = texelFetch(prev_denoise_variance, p, 0).x;
        float Li = dot(Ci.xyz, LUM_COEFF);
        vec3  Ni = ph_decode_lighting_normal(texelFetch(restir_normal_history, p, 0));
        float Di = ph_linearize_depth(texelFetch(depthtex0, PH_DEPTH_MODIFIER(p), 0).x);
        float k  = kernel[i];

        // Color (luminance) weight
        float wC = luma_edge_stopping_weight(L0, Li, phi_luminance);

        // Normal weight
        float wN = normal_edge_stopping_weight(N0, Ni);

        // Position weight
        float wP = depth_edge_stopping_weight(D0, Di, phi_depth);

        float w = wC * wN * wP * k;
        W_sum += w;
        C_sum += Ci.xyz * w;
        V_sum += Vi * w * w;
    }

    W_sum = max(0.0001f, W_sum);
    V_sum = max(0.0001f, V_sum);

    color_out = C_sum / W_sum;
    variance_out = max(V_sum / (W_sum * W_sum), 0.0f);
}
