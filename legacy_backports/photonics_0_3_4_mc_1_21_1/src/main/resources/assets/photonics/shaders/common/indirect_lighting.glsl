// TODO: take sun light into account
vec3 ph_sample_indirect_impl() {
    ray.result_position = rt_pos;
    ray.result_normal = block_normal;

    //    bool is_axis_aligned = is_axis_aligned(base_normal);

    // If normal is not axis aligned, sample both hemispheres
    //    if (!is_axis_aligned) {
    //        light_ray.result_normal = vec3(0.0f);
    //    }

    vec3 indirect_color = vec3(1.0f);

    #ifndef PH_RESTIR_COMBINED_GI
    for (int i = 0; i < 2; i++) {
    #else
    for (int i = 0; i < 4; i++) {
    #endif
        lightEmittance = vec3(0.0f);
        ray.origin = ray.result_position + 0.1f * ray.result_normal;
        // TODO: use blue noise
        ray.direction = normalize(ray.result_normal + ph_sample_random_direction(rng_state));

        bool sun = ph_RandomFloat01(rng_state) < 0.25f && dot(ph_sun_direction, ray.result_normal) > 0.707f;
        if (sun) {
            ray.direction = ph_sun_direction;
        }

        breakOnEmpty = true;
        trace_ray(ray, true);
        breakOnEmpty = false;

        indirect_color*= result_tint_color;
        if (!ray.result_hit && !ray_iteration_bound_reached) {
            if (!sun) {
                indirect_color *= (float(i > 0) * 2 + 2) * indirect_light_color;
            } else {
                indirect_color *= (float(i > 0) * 15 + 1) * indirect_light_color;
            }

            return indirect_color;
        } else if (dot(lightEmittance, lightEmittance) > 0.0f) {
            indirect_color *= 2.0f * lightEmittance;
            //            indirect_color = vec3(0.0f);

            return indirect_color;
        }

        indirect_color *= ray.result_color;
    }

    return vec3(0.0f);
}
