#include "/photonics/interface/lighting_interface.glsl"
#include "/photonics/tracing.glsl"
#include "/photonics/utility/random.glsl"

//TODO: Make these into settings
#define PH_MAX_GI_BOUNCES 1
#define PH_MAX_GI_ITERATIONS 100

bool ph_should_trace_to_sun(
    inout uint rnd_state,
    vec3 surface_rt_pos,
    vec3 surface_normal,
    vec3 sun_direction
) {
    return ph_rand_next_float(rnd_state) < 0.6f &&
        dot(sun_direction, surface_normal) >= 0.707f &&
        is_in_shadow_at(surface_rt_pos - rt_camera_position, surface_normal);
}

vec3 next_direction(
    inout uint rnd_state,
    out bool is_sun,
    vec3 sun_direction,
    vec3 surface_rt_pos,
    vec3 surface_normal
) {
    if (ph_should_trace_to_sun(rnd_state, surface_rt_pos, surface_normal, sun_direction)) {
        is_sun = true;
        return sun_direction;
    } else {
        is_sun = false;
        return ph_rand_direction(rnd_state, surface_normal);
    }
}

void sample_indirect(
    inout vec3 indirect_color,
    vec3 sample_rt_pos,
    vec3 geo_normal,
    inout uint rnd_state
) {

    vec4 running_tint_color = vec4(0.0f);
    float running_light_transmittance = 1.0f;

    vec3 running_bounce_color = vec3(1.0f);
    vec3 sun_direction = get_sun_direction();

    int bounce_count = 0;
    bool is_tracing_sun = false;

    RayIterator ray;
    ray_iter_begin(
        ray,
        sample_rt_pos,
        next_direction(rnd_state, is_tracing_sun, sun_direction, sample_rt_pos, geo_normal)
    );
    ray.iterations = PH_MAX_GI_ITERATIONS;

    ray.position += ray.direction * 0.1f;


    RayResult last_hit = missed_ray_result();
    while (ray.iterations != 0) {
        last_hit = ray_iter_next(ray);

        VoxelData voxel_data;
        vec4 albedo;

        // albedo fetching
        if (ray_result_is_hit(last_hit)) {
            voxel_data = ray_result_voxel_data(last_hit);
            albedo = voxel_data_albedo(voxel_data);

            if (ray_result_is_transparent(last_hit)) {
                running_light_transmittance *= 1.0f - albedo.a;
                ray_iter_apply_transparency(running_tint_color, albedo);
                ray_iter_skip_block(ray);

                continue;
            }
        }

        vec3 nee_color;
        if (ray_result_is_hit(last_hit) && ray_iter_is_in_bounds(ray)) {
            Light hit_light = ray_result_light_data(last_hit);
            if (light_is_valid(hit_light) && hit_light.type == LIGHT_TYPE_NOT_TRACED) {
                nee_color = light_sample_at(
                    hit_light,
                    sample_rt_pos,
                    ray_result_position(last_hit),
                    -geo_normal,
                    -geo_normal
                ) * 3.0f;
            } else nee_color = vec3(0.0f);
        } else {
            albedo = vec4(1.0f);
            ray.iterations = 0;
            nee_color = is_tracing_sun ? get_sun_color() : get_sky_color() * 0.4f;
        }

        if (nee_color != vec3(0.0f)) {
            if (running_tint_color != vec4(0.0))
                nee_color *= running_tint_color.rgb;

            indirect_color += nee_color * running_bounce_color * running_light_transmittance;
        }

        running_bounce_color *= albedo.rgb;

        bounce_count += 1;
        if (ray.iterations == 0 || bounce_count > PH_MAX_GI_BOUNCES) return;

        sample_rt_pos = ray_result_position(last_hit);
        geo_normal = ray_result_normal(last_hit);

        ray_iter_set_direction(
            ray,
            next_direction(rnd_state, is_tracing_sun, sun_direction, sample_rt_pos, geo_normal)
        );
        ray.position += ray.direction * 0.1f;
    }
}