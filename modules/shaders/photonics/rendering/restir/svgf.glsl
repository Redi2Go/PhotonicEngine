#include "/photonics/modifiers/restir_denoiser_depth_fetch_modifier.glsl"

#ifdef PH_RESTIR_DENOISER_DEPTH_FETCH_MODIFIER_DISABLED
#define SVGF_DEPTH_MODIFIER(p) p
#else
#define SVGF_DEPTH_MODIFIER(p) modify_denoiser_depth_fetch(p)
#endif

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



float svgf_normal_edge_stopping_weight(vec3 center_normal, vec3 sample_normal)
{
    const float power = 128.0f;

    return pow(clamp(dot(center_normal, sample_normal), 0.0f, 1.0f), power);
}

float svgf_depth_edge_stopping_weight(float center_depth, float sample_depth, float phi)
{
    return exp(-abs(center_depth - sample_depth) / phi);
}

float svgf_luma_edge_stopping_weight(float center_luma, float sample_luma, float phi)
{
    return exp(-abs(center_luma - sample_luma) / phi);
}

float svgf_linearize_depth(float d)
{
    return near * far / (far + d * (near - far));
}
