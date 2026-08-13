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

float indirect_sample_normal_factor(FragData frag, vec3 hit_pos) {
    const float c_pi = 3.14159265359f;
    const float rcp_pi = 1.0f / c_pi;

    vec3 hit_dir = normalize(hit_pos - frag_data_rt_pos(frag));
    return clamp(dot(frag_data_tex_normal(frag), hit_dir), 0.0001f, 1.0f) * rcp_pi;
}

float indirect_sample_compute_jacobian(IndirectSample smple, vec3 dst_pos, vec3 src_pos) {
    vec3 hit_position = indirect_sample_get_hit_point(smple);

    vec3 to_current = dst_pos - hit_position;
    vec3 to_source  = src_pos - hit_position;

    float to_current_sq = dot(to_current, to_current);
    float to_source_sq = dot(to_source, to_source);

    vec3 hit_normal = indirect_sample_get_hit_normal(smple);

    float jacobian = (dot(hit_normal, to_current * inversesqrt(to_current_sq)) / to_current_sq);
    jacobian /= (dot(hit_normal, to_source * inversesqrt(to_source_sq)) / to_source_sq);

    return isinf(jacobian) || isnan(jacobian) ? 0.0f : jacobian;
}

float indirect_sample_compute_shift(IndirectSample smple, FragData dst_frag, FragData src_frag, float limit) {
    vec3 hit_point = indirect_sample_get_hit_point(smple);
    float occlusion_old = indirect_sample_normal_factor(src_frag, hit_point);
    float occlusion_new = indirect_sample_normal_factor(dst_frag, hit_point);

    float occlusion_factor = occlusion_new / occlusion_old;
    float jacobian_factor = indirect_sample_compute_jacobian(smple, frag_data_rt_pos(dst_frag), frag_data_rt_pos(src_frag));

    if (occlusion_factor > limit) return -1.0f;
    if (jacobian_factor > limit) return -1.0f;

    return jacobian_factor * occlusion_factor;
}
