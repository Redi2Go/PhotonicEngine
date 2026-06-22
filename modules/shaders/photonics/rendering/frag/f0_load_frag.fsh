#version 430

#include "/photonics/rendering/frag/world_interface.glsl"
#include "/photonics/utility/normal_encoding.glsl"
#include "/photonics/rendering/frag/frag_data.glsl"

layout(location = 0) out vec4 frag_data0_out;
layout(location = 1) out vec4 frag_data1_out;

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

void ph_encode_frag(out vec4 data0, out uvec4 data1) {
    data0 = vec4(0.0f);
    data1 = uvec4(0u);

    if (!is_in_world()) return;

    #define frag_player_pos data0.xyz

    vec3 frag_geo_normal;
    vec3 frag_tex_normal;
    vec3 frag_rt_pos;

    bool frag_is_hand;
    bool frag_is_bad_angle;

    load_frag_data(frag_geo_normal, frag_tex_normal, frag_player_pos, frag_rt_pos, frag_is_hand, frag_is_bad_angle);

    // Position encoding

    data0.xyz = frag_player_pos;

    vec3 to_rt = frag_rt_pos - (frag_player_pos + rt_camera_position);
    float dist_sq = dot(to_rt, to_rt);
    float dist_inv = inversesqrt(dist_sq);

    to_rt *= dist_inv;
    dist_sq *= dist_inv;

    data0.w = dist_sq;
    data1.x = packSnorm2x16(ph_encode_normal(to_rt));


    // Normal encoding

    data1.y = packSnorm2x16(ph_encode_normal(frag_geo_normal));
    data1.z = packSnorm2x16(ph_encode_normal(frag_tex_normal));

    data1.w |= frag_is_in_world_bit;
    data1.w |= frag_is_bad_angle ? frag_bad_angle_bit : 0;
    data1.w |= frag_is_hand ? frag_is_hand_bit : 0;
}

void main() {
    vec4 data0;
    uvec4 data1;
    ph_encode_frag(data0, data1);

    frag_data0_out = data0;
    frag_data1_out = uintBitsToFloat(data1);
}

