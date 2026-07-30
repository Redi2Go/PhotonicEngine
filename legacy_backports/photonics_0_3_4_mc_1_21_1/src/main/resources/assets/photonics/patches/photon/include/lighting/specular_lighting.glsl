#file "/include/lighting/specular_lighting.glsl"

#replace "#include "/include/utility/space_conversion.glsl""
#include "/include/utility/space_conversion.glsl"

#if defined WORLD_SPACE_REFLECTIONS && defined USE_RT && defined ENVIRONMENT_REFLECTIONS
#include "/photonics/photonics.glsl"
#define USE_WSR
#endif
#endreplace

#replace "vec3 trace_specular_ray("
vec3 sky_tint_color = vec3(1f);

vec3 trace_screen_specular_ray(
#endreplace

#replace "vec3 sky_reflection = get_sky_reflection(ray_dir, skylight, hit_pos);"
vec3 sky_reflection = get_sky_reflection(ray_dir, skylight, hit_pos) * sky_tint_color;
#endreplace

#replace "vec3 get_specular_reflections("
#ifdef USE_WSR
vec3 trace_world_specular_ray(
    out bool hit,
    vec3 screen_pos,
    vec3 world_pos,
    vec3 flat_normal,
    vec3 ray_dir
) {
    RayJob ray = RayJob(
        world_pos - world_offset + 0.001 * flat_normal,
        ray_dir,
        vec3(0), vec3(0), vec3(0), false
    );

    RAY_ITERATION_COUNT = WSR_RAY_ITERATIONS;
    trace_ray(ray, true);

    hit = ray.result_hit;
    const vec3 hit_pos = vec3(0.0);

    if (!hit) return vec3(0f);

    vec3 hit_color = ray.result_color;
    vec3 hit_flat_normal = ray.result_normal;
    vec3 hit_pos_world = ray.result_position + world_offset;
    vec3 hit_pos_scene = ray.result_position - rt_camera_position;
    float hit_sky = get_result_sky_light(hit_flat_normal) / 15f;
    vec2 hit_light_levels = vec2(0, hit_sky);

    uint hit_material_mask = uint(max(result_block_id - 10000, 0));
    vec2 hit_uv_prev = reproject_scene_space(hit_pos_scene, false, false).xy;

    Material hit_material = material_from(
        hit_color,
        hit_material_mask,
        hit_pos_world,
        hit_flat_normal,
        hit_light_levels
    );

#if defined WORLD_OVERWORLD && defined CLOUD_SHADOWS
    float cloud_shadows = get_cloud_shadows(colortex8, hit_pos_scene);
#else
    const float cloud_shadows = 1.0;
#endif

#if (defined WORLD_OVERWORLD || defined WORLD_END) && defined SHADOW
    float shadow_distance_fade = 0.0;
    float sss_depth = 0.0;

    vec3 shadows = get_filtered_shadows(
        hit_pos_scene,
        hit_flat_normal,
        hit_light_levels.y,
        cloud_shadows,
        hit_material.sss_amount,
        shadow_distance_fade,
        sss_depth
    );
#else
    const vec3 shadows = vec3(1.0);
    const float shadow_distance_fade = 1.0;
    const float sss_depth = 0.0;
#endif

    vec3 direction_world = normalize(hit_pos_scene - gbufferModelViewInverse[3].xyz);

    float NoL = dot(hit_flat_normal, light_dir);
    float NoV = clamp01(dot(hit_flat_normal, -direction_world));
    float LoV = dot(light_dir, -direction_world);
    float halfway_norm = inversesqrt(2.0 * LoV + 2.0);
    float NoH = (NoL + NoV) * halfway_norm;

    vec3 reflection = get_diffuse_lighting(
        #ifdef USE_RT
        false,
        #endif
        hit_material,
        hit_pos_scene,
        hit_flat_normal,
        hit_flat_normal,
        hit_flat_normal,
        shadows,
        hit_light_levels,
        1, // ao
        1, // ambient_sss,
        sss_depth,
#ifdef CLOUD_SHADOWS
        cloud_shadows,
#endif
#ifdef SHADOW_SSRT
            0,
#else
            shadow_distance_fade,
#endif
        NoL,
        NoV,
        NoH,
        LoV
    );

#if defined WORLD_OVERWORLD
#ifdef VL
    // Intended to make reflected fog better match VL
    // Assumption is that if there is a hit and the hit object is vaguely in
    // the direction of the sun then the fog would be shadowed by the hit
    // object
    float fog_shadow = hit ? 1.0 - sqr(max0(dot(light_dir, ray_dir))) : 1.0;
#else
    const float fog_shadow = 1.0;
#endif

    // Apply analytic fog in reflection
    mat2x3 analytic_fog = air_fog_analytic(
        world_pos,
        #ifdef USE_WSR
        hit_pos_world,
        #else
        hit_pos_scene + cameraPosition,
        #endif
        false,
        eye_skylight,
        fog_shadow
    );

    reflection = reflection * analytic_fog[1] + analytic_fog[0];
#endif

    return reflection * result_tint_color;
}

vec3 trace_specular_ray(
    vec3 flat_normal,
    vec3 screen_pos,
    vec3 view_pos,
    vec3 world_pos,
    vec3 ray_dir,
    float dither,
    float skylight,
    uint intersection_step_count,
    uint refinement_step_count,
    int mip_level
) {
#ifdef ENVIRONMENT_REFLECTIONS
    bool wsr_hit;
    vec3 reflection = trace_world_specular_ray(
        wsr_hit,
        screen_pos,
        world_pos,
        flat_normal,
        ray_dir
    );

    if (wsr_hit) return reflection;

    sky_tint_color = result_tint_color;
#else
    sky_tint_color = vec3(1f);
#endif

    return trace_screen_specular_ray(
        screen_pos,
        view_pos,
        world_pos,
        ray_dir,
        dither,
        #ifdef ENVIRONMENT_REFLECTIONS
        1,
        #else
        skylight,
        #endif
        intersection_step_count,
        refinement_step_count,
        mip_level
    );
}

#else
#define trace_specular_ray trace_screen_specular_ray
#endif

vec3 get_specular_reflections(
#endreplace

#replace "vec3 radiance = trace_specular_ray("
vec3 radiance = trace_specular_ray(
#ifdef USE_WSR
    flat_normal,
#endif
#endreplace

#replace "vec3 reflection = trace_specular_ray("
vec3 reflection = trace_specular_ray(
    #ifdef USE_WSR
    flat_normal,
    #endif
#endreplace