#version 430

//ph_Required: uniform float near, far;

#include "/photonics/rendering/frag/world_interface.glsl"
#include "/photonics/utility/normal_encoding.glsl"
#include "/photonics/rendering/frag/frag_data.glsl"
#include "/photonics/rendering/frag/fast_data.glsl"

// Deprecated: Remove for 0.4 release
#include "/photonics/rendering/frag/depth.glsl"


layout(location = 0) out vec4  frag_data0_out;
layout(location = 1) out uvec4 frag_data1_out;
layout(location = 2) out vec2  fast_data_out;

void load_frag_data(
    out vec3 frag_geo_normal,
    out vec3 frag_tex_normal,

    out vec3 frag_player_pos,
    out vec3 frag_rt_pos,

    out bool frag_is_hand,
    out bool frag_is_bad_angle
) {
    load_fragment_data(frag_geo_normal, frag_tex_normal);
    frag_is_hand = is_hand_at();

    frag_player_pos = load_player_position();
    frag_rt_pos = frag_player_pos + rt_camera_position;

    float dist = distance(floor(frag_rt_pos), floor(rt_camera_position));
    float resolution = ceil((dist / 16)) / (4 * PH_RENDER_SCALE);

    frag_is_bad_angle = dot(frag_geo_normal, normalize(frag_rt_pos - rt_camera_position)) > -0.2f && dist > 16.0f;

    // Attempts to correct bias from depth
    frag_rt_pos += frag_geo_normal * (0.01f + (0.04f * resolution));
}

float ph_linearize_depth(float d) {
    return near * far / (far + d * (near - far));
}

void main() {
    const float infinity = intBitsToFloat(0x7f800000);

    frag_data0_out = vec4(0.0f);
    frag_data1_out = uvec4(0u);
    fast_data_out = vec2(infinity, 0.0f);

    if (!is_in_world()) return;

    float depth = load_depth();
    #define frag_player_pos frag_data0_out.xyz

    vec3 frag_geo_normal;
    vec3 frag_tex_normal;
    vec3 frag_rt_pos;

    bool frag_is_hand;
    bool frag_is_bad_angle;

    load_frag_data(frag_geo_normal, frag_tex_normal, frag_player_pos, frag_rt_pos, frag_is_hand, frag_is_bad_angle);
    fast_frag_encode(ph_linearize_depth(depth), frag_geo_normal, frag_tex_normal, fast_data_out);

    // Position encoding

    frag_data0_out.xyz = frag_player_pos;

    vec3 to_rt = frag_rt_pos - (frag_player_pos + rt_camera_position);
    float dist_sq = dot(to_rt, to_rt);
    float dist_inv = inversesqrt(dist_sq);

    to_rt *= dist_inv;
    dist_sq *= dist_inv;

    frag_data0_out.w = dist_sq;
    frag_data1_out.x = ph_pack_normal(to_rt);


    // Normal encoding

    frag_data1_out.y = ph_pack_normal(frag_geo_normal);
    frag_data1_out.z = ph_pack_normal(frag_tex_normal);

    frag_data1_out.w |= frag_is_in_world_bit;
    frag_data1_out.w |= frag_is_bad_angle ? frag_bad_angle_bit : 0;
    frag_data1_out.w |= frag_is_hand ? frag_is_hand_bit : 0;
}

