//ph_required: uniform sampler2D ph_frag_data0;
//ph_required: uniform sampler2D ph_frag_data1;

//ph_required: uniform sampler2D prev_ph_frag_data0;
//ph_required: uniform sampler2D prev_ph_frag_data1;

#include "/photonics/utility/normal_encoding.glsl"

struct FragData {
    vec4 data0;
    uvec4 data1;
};

const uint frag_is_in_world_bit = 1u << 0;
const uint frag_bad_angle_bit = 1u << 1;
const uint frag_is_hand_bit = 1u << 2;

void frag_data_load(out FragData frag, ivec2 texel) {
    frag.data0 = texelFetch(ph_frag_data0, texel, 0);
    frag.data1 = floatBitsToUint(texelFetch(ph_frag_data1, texel, 0));
}

void frag_data_load_previous(out FragData frag, ivec2 texel) {
    frag.data0 = texelFetch(prev_ph_frag_data0, texel, 0);
    frag.data1 = floatBitsToUint(texelFetch(prev_ph_frag_data1, texel, 0));
}

vec3 frag_data_player_pos(FragData frag) {
    return frag.data0.xyz;
}

vec3 frag_data_rt_pos(FragData frag) {
    vec3 dir = ph_decode_normal(unpackSnorm2x16(frag.data1.x));
    float mag = frag.data0.w;

    return (frag.data0.xyz + rt_camera_position) + (dir * mag);
}

vec3 frag_data_geo_normal(FragData frag) {
    return ph_decode_normal(unpackSnorm2x16(frag.data1.y));
}

vec3 frag_data_tex_normal(FragData frag) {
    return ph_decode_normal(unpackSnorm2x16(frag.data1.z));
}

bool frag_data_is_in_world(FragData frag) {
    return (frag.data1.w & frag_is_in_world_bit) != 0;
}

bool frag_data_is_bad_angle(FragData frag) {
    return (frag.data1.w & frag_bad_angle_bit) != 0;
}

bool frag_data_is_hand(FragData frag) {
    return (frag.data1.w & frag_is_hand_bit) != 0;
}
