#include "/photonics/tracing.glsl"
#include "/photonics/utility/random.glsl"
#include "/photonics/utility/normal_encoding.glsl"

struct IndirectSample {
    vec3 hit_point;
    uint packed_hit_normal;
    bool hit_sky;

    vec3 color;
};

const float indirect_sky_distance = 10000.0f;

IndirectSample indirect_sample_empty() {
    return IndirectSample(vec3(0.0f), 0u, true, vec3(0.0f));
}

void indirect_sample_set_color(inout IndirectSample smple, vec3 color) {
    smple.color = color;
}

vec3 indirect_sample_get_hit_normal(IndirectSample smple) {
    return ph_decode_normal(unpackUnorm2x16(smple.packed_hit_normal));
}

void indirect_sample_set_hit_normal(inout IndirectSample smple, vec3 hit_normal) {
    smple.packed_hit_normal = packUnorm2x16(ph_encode_normal(hit_normal));
}

vec3 indirect_sample_get_hit_point(IndirectSample smple) {
    return smple.hit_point + rt_camera_position;
}

void indirect_sample_set_hit_point(
        inout IndirectSample smple,
        vec3 hit_position,
        vec3 visible_point,
        vec3 visible_normal,
        uint rnd_state
) {
    if (isinf(hit_position.x)) {
        vec3 direction = ph_rand_direction(rnd_state, visible_normal);

        smple.hit_point = visible_point + (direction * indirect_sky_distance);
        smple.hit_sky = true;
    } else {
        smple.hit_point = hit_position;
        smple.hit_sky = false;
    }

    smple.hit_point -= rt_camera_position;
}


float indirect_sample_weight(IndirectSample smple) {
    return ph_luminance(smple.color);
}

float indirect_sample_compute_jacobian(IndirectSample smple, FragData src_frag) {
    #define dst_pos frag_player_pos
    #define dst_geo_normal frag_geo_normal
    #define dst_tex_normal frag_tex_normal

    #define src_pos frag_data_player_pos(src_frag)
    #define src_geo_normal frag_data_geo_normal(src_frag)
    #define src_tex_normal frag_data_tex_normal(src_frag)

    #define hit_pos smple.hit_point
    #define hit_normal indirect_sample_get_hit_normal(smple)

    vec3 to_dst = hit_pos - dst_pos;
    float to_dst_sq = dot(to_dst, to_dst);
    float to_dst_inv = inversesqrt(to_dst_sq);

    float jacobian     = dot(hit_normal, to_dst * to_dst_inv) / to_dst_sq;
    float normal_shift = dot(dst_tex_normal, -to_dst * to_dst_inv) / dot(dst_geo_normal, -to_dst * to_dst_inv);

    vec3 to_src = hit_pos - src_pos;
    float to_src_sq = dot(to_src, to_src);
    float to_src_inv = inversesqrt(to_src_sq);

    jacobian     /= dot(hit_normal, to_src * inversesqrt(to_src_sq)) / to_src_sq;
    normal_shift /= dot(src_tex_normal, -to_src * to_src_inv) / dot(src_geo_normal, -to_src * to_src_inv);

    return jacobian * normal_shift;

    #undef dst_pos
    #undef dst_tex_normal
    #undef src_pos
    #undef src_tex_normal
    #undef hit_pos
    #undef hit_normal
}
