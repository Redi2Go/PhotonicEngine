#include "/photonics/tracing.glsl"
#include "/photonics/utility/random.glsl"
#include "/photonics/utility/normal_encoding.glsl"

struct IndirectSample {
    uint packed_visible_normal;
    uint packed_hit_normal;

    vec3 hit_position;
    float hit_distance;

    vec3 color;
    uint rnd_state;
};


IndirectSample indirect_sample_empty() {
    return IndirectSample(0u, 0u, vec3(0.0f), 0u, vec3(0.0f), 0u);
}

vec3 indirect_sample_get_visible_normal(IndirectSample smple) {
    return ph_decode_normal(unpackUnorm2x16(smple.packed_visible_normal));
}

void indirect_sample_set_visible_normal(inout IndirectSample smple, vec3 visible_normal) {
    smple.packed_visible_normal = packUnorm2x16(ph_encode_normal(visible_normal));
}

vec3 indirect_sample_get_hit_normal(IndirectSample smple) {
    return ph_decode_normal(unpackUnorm2x16(smple.packed_hit_normal));
}

void indirect_sample_set_hit_normal(inout IndirectSample smple, vec3 hit_normal) {
    smple.packed_hit_normal = packUnorm2x16(ph_encode_normal(hit_normal));
}

bool indirect_sample_traced_sky(IndirectSample smple) {
    return isnan(smple.hit_position.x);
}

vec3 indirect_sample_get_visible_point(IndirectSample smple) {
    #define smple_visible_normal indirect_sample_get_visible_normal(smple)
    #define smple_hit_normal indirect_sample_get_hit_normal(smple)

    vec3 direction = ph_rand_direction(smple.rnd_state, smple_visible_normal);
    return smple.hit_position - (direction * smple.hit_distance);
}

float indirect_sample_compute_jacobian(IndirectSample smple, vec3 rt_pos) {
    if (indirect_sample_traced_sky(smple)) return 1.0f;

    vec3 to_current = rt_pos - smple.hit_position;
    vec3 to_source = indirect_sample_get_visible_point(smple) - smple.hit_position;

    float to_current_sq = dot(to_current, to_current);
    float to_source_sq = dot(to_source, to_source);

    vec3 hit_normal = indirect_sample_get_hit_normal(smple);

    float jacobian = (dot(hit_normal, to_current * inversesqrt(to_current_sq)) / to_current_sq);
         jacobian /= (dot(hit_normal, to_source * inversesqrt(to_source_sq)) / to_source_sq);

    return isinf(jacobian) || isnan(jacobian) ? 0.0f : jacobian;
}

vec3 indirect_sample_validate_visibility(inout IndirectSample smple, vec3 rt_pos) {
    if (indirect_sample_traced_sky(smple)) return smple.color;

    vec3 hit_position = smple.hit_position;

    RayIterator ray;
    ray_iter_begin(ray, rt_pos, hit_position - rt_pos);

    RayResult hit = missed_ray_result();
    while (ray_iter_has_next(ray)) {
        hit = ray_iter_next(ray);

        if (ray_result_is_transparent(hit)) {
            ray_iter_skip_block(ray);
            continue;
        }
    }

    vec3 diff = ray_result_position(hit) - hit_position;
    float new_dist = dot(diff, diff);

    return new_dist >= 0.05f ? vec3(0.0f) : smple.color;
}
