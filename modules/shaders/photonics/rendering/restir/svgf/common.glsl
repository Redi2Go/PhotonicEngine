#include "/photonics/utility/normal_encoding.glsl"

#define SVGF_DENOISE_OUT 0
//ph_required: uniform usampler2D prev_denoise_result;

// 3×3 Gaussian Kernel & Offsets
const float kernel[9] = float[](
        1.0 / 6., 2.0 / 3., 1.0 / 6.,
        2.0 / 3., 1.0, 2.0 / 3.,
        1.0 / 6., 2.0 / 3., 1.0 / 6.
);

const ivec2 offset[9] = ivec2[](
        ivec2(-1, -1), ivec2(0, -1), ivec2(1, -1),
        ivec2(-1, 0), ivec2(0, 0), ivec2(1, 0),
        ivec2(-1, 1), ivec2(0, 1), ivec2(1, 1)
);

#define SVGF_CENTER_INDEX 4

struct SvgfSample {
    vec3 color;
    float variance;

    float depth;
    float age;

    uint packed_normal;
    bool is_hand;
};

SvgfSample svgf_sample_empty() {
    return SvgfSample(vec3(0.0f), 0.0f, 1.0f, 0.0f, 0u, false);
}

vec3 svgf_sample_get_normal(SvgfSample smple) {
    return ph_unpack_normal(smple.packed_normal);
}

void svgf_sample_decode(out SvgfSample smple, uvec4 value) {
    const uint sign_bit = 0x80000000u;
    vec2 unpacked = unpackHalf2x16(value.x & ~sign_bit);
    smple.color.rg = unpacked;

    unpacked = unpackHalf2x16(value.y);
    smple.color.b = unpacked.x;
    smple.variance = unpacked.y;

    unpacked = unpackUnorm2x16(value.z);
    smple.depth = unpacked.x;
    smple.age = unpacked.y * PH_RESTIR_ACCUMULATION_FRAMES;

    smple.packed_normal = value.w;
    smple.is_hand = (value.x & sign_bit) != 0;
}

void svgf_sample_encode(SvgfSample smple, out uvec4 value) {
    const uint sign_bit = 0x80000000u;

    value.x = packHalf2x16(smple.color.rg) | (smple.is_hand ? sign_bit : 0);
    value.y = packHalf2x16(vec2(smple.color.b, smple.variance));
    value.z = packUnorm2x16(vec2(smple.depth, smple.age / PH_RESTIR_ACCUMULATION_FRAMES));
    value.w = smple.packed_normal;
}

void svgf_sample_load(out SvgfSample smple, ivec2 tex_coord) {
    svgf_sample_decode(smple, texelFetch(prev_denoise_result, tex_coord, 0));
}

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

float svgf_shadow_stopping_weight(float center_vis, float sample_vis, float phi)
{
    return exp(-abs(center_vis - sample_vis) / phi);
}
