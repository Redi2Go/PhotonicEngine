#include "/photonics/tracing.glsl"
#include "/photonics/utility/random.glsl"

struct IndirectSample {
    vec3 visible_normal;

    vec3 hit_position;
    vec3 hit_normal;
    float hit_distance;

    bool traced_sky;
    vec3 color;

    uint rnd_state;
};

IndirectSample indirect_sample_empty() {
    return IndirectSample(vec3(0.0f), vec3(0.0f), vec3(0.0f), 0.0f, false, vec3(0.0f), 0u);
}

vec3 indirect_sample_get_visible_point(IndirectSample smple) {
    vec3 direction = ph_rand_direction(smple.rnd_state, smple.visible_normal);
    return smple.hit_position - (direction * smple.hit_distance);
}

float indirect_sample_clamp_jacobian(float value) {
    return value > 10.0f || value < 0.1f ? 0.0f : clamp(value, 0.1, 3.0f);
}

float indirect_sample_compute_jacobian(
    IndirectSample smple,
    vec3 player_pos,
    vec3 geo_normal
) {
    if (smple.traced_sky) return 1.0f;
    if (frag_is_hand) return 1.0f;

    vec3 vec_new = player_pos - smple.hit_position;
    vec3 vec_old = indirect_sample_get_visible_point(smple) - smple.hit_position;

    float new_dist_sq = dot(vec_new, vec_new);
    float old_dist_sq = dot(vec_old, vec_old);

    float new_cosine = clamp(
        dot(
            smple.hit_normal,
            vec_new * inversesqrt(max(new_dist_sq, 1e-30f))
        ),
        0.0f,
        1.0f
    );

    float old_cosine = clamp(
        dot(
            smple.hit_normal,
            vec_old * inversesqrt(max(old_dist_sq, 1e-30f))
        ),
        0.0f,
        1.0f
    );

    float jacobian = (new_cosine * old_dist_sq) / (old_cosine * new_dist_sq);
    if (isinf(jacobian) || isnan(jacobian)) jacobian = 0.0f;

    return indirect_sample_clamp_jacobian(jacobian);
}

vec3 indirect_sample_validate_visibility(inout IndirectSample smple, vec3 rt_pos) {
    if (smple.traced_sky) return smple.color;

    vec3 hit_position = smple.hit_position + rt_camera_position;

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
