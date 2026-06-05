#include "/photonics/interface/lighting_interface.glsl"
#include "/photonics/tracing.glsl"
#include "/photonics/utility/random.glsl"

#include "/photonics/modifiers/indirect_surface_sample_modifier.glsl"

//TODO: Make these into settings
#define PH_MAX_GI_ITERATIONS 100

bool ph_should_trace_to_sun(
    inout uint rnd_state,
    int bounce,
    vec3 surface_rt_pos,
    vec3 surface_normal
) {
#if !defined NO_SHADOW_MAPPING
    return bounce != -1 &&
        ph_rand_next_float(rnd_state) < 0.6f &&
        dot(get_sun_direction(), surface_normal) >= -0.01f &&
        !is_in_shadow_at(surface_rt_pos - rt_camera_position, surface_normal);
#else
        return ph_rand_next_float(rnd_state) < 0.25f &&
            dot(get_sun_direction(), surface_normal) >= 0.707f;
#endif
}

vec3 ph_next_direction(
    inout uint rnd_state,
    out bool is_sun,
    int bounce,
    vec3 surface_rt_pos,
    vec3 surface_normal
) {
    if (ph_should_trace_to_sun(rnd_state, bounce, surface_rt_pos, surface_normal)) {
        is_sun = true;
        return get_sun_direction();
    } else {
        is_sun = false;
        return ph_rand_direction(rnd_state, surface_normal);
    }
}

void ph_gi_prepare_ray(
    inout RayIterator ray,
    inout uint rnd_state,
    int bounce,

    vec3 rt_pos,
    vec3 geo_normal,

    out bool is_tracing_sun
) {
    ray_iter_set_direction(
        ray,
        ph_next_direction(
            rnd_state,
            is_tracing_sun,
            bounce,
            rt_pos,
            geo_normal
        )
    );

    ray_iter_offset_position(ray, ray.direction * 0.01f);
}

void sample_indirect(
    inout vec3 indirect_color,
    vec3 sample_rt_pos,
    vec3 geo_normal,
    vec3 tex_normal,
    inout uint rnd_state,

    out vec3 first_hit,
    out vec3 first_normal
) {
    vec4 running_tint_color = vec4(0.0f);
    float running_light_transmittance = 1.0f;

    vec3 running_bounce_color = vec3(1.0f);

    int bounce_count = -1;
    bool is_tracing_sun = false;

    RayIterator ray;

    ray.iterations = PH_MAX_GI_ITERATIONS;
    ray_iter_set_position(ray, sample_rt_pos);
    ph_gi_prepare_ray(ray, rnd_state, bounce_count, sample_rt_pos, geo_normal, is_tracing_sun);

    while (bounce_count < PH_MAX_GI_BOUNCES) {
        RayResult hit = ray_iter_next(ray);
        if (bounce_count == -1) {
            first_hit = ray_result_is_hit(hit) ? ray_result_position(hit) : vec3(-1.0f);
            first_normal = ray_result_normal(hit);
        }

        // No hit & not out of bounds means we likely out of iterations
        if (!ray_result_is_hit(hit) && ray_iter_is_in_bounds(ray)) break;

        vec4 albedo = vec4(1.0f);
        vec3 light_color = vec3(0.0f);

        vec3 hit_position = ray_result_position(hit);

        // Ray either hit something or reached sky
        if (ray_result_is_hit(hit)) {
            VoxelData voxel_data = ray_result_voxel_data(hit);
            albedo = voxel_data_albedo(voxel_data);

            if (ray_result_is_transparent(hit)) {
                // Multiply alpha by 0.25 as it looks better with glass
                running_light_transmittance *= 1.0f - (albedo.a * 0.25f);
                ray_iter_apply_transparency(running_tint_color, albedo);
                ray_iter_skip_block(ray);

                continue;
            }

#if defined PH_INDIRECT_SURFACE_SAMPLE_MODIFIER_DISABLED
            Light hit_light = ray_result_light_data(hit);
            if (light_is_valid(hit_light) && hit_light.type == LIGHT_TYPE_NOT_TRACED) {
                light_color = light_sample_at(
                    hit_light,
                    sample_rt_pos,
                    floor(ray_result_position(hit)) + 0.5f,
                    geo_normal,
                    geo_normal
                ) * 3.0f;
            }
#else
            light_color = modify_indirect_surface_sample(
                hit,
                sample_rt_pos,
                geo_normal,
                bounce_count,
                rnd_state
            );
#endif
        } else {
            ray.iterations = 0;
            vec3 player_pos = hit_position - rt_camera_position;

            light_color = is_tracing_sun ? get_sun_color(player_pos, ray.direction) : get_sky_color(player_pos, ray.direction);
        }

        if (light_color != vec3(0.0f)) {
            vec3 gi_tint_color = running_tint_color != vec4(0.0) ? running_tint_color.rgb : vec3(1.0f);
            vec3 gi_bounce_color = running_bounce_color;
            float gi_intensity = running_light_transmittance;

            indirect_color += light_color * gi_tint_color * gi_bounce_color * gi_intensity;
        }

        bounce_count += 1;
        running_bounce_color *= albedo.rgb;

        sample_rt_pos = hit_position;
        geo_normal = ray_result_normal(hit);

        ph_gi_prepare_ray(ray, rnd_state, bounce_count, sample_rt_pos, geo_normal, is_tracing_sun);
    }
}
