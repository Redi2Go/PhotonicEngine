#include "/photonics/modifiers/handheld_light_pulse_modifier.glsl"
#include "/photonics/light.glsl"
#include "/photonics/tracing.glsl"

#define PH_HANDHELD_RAY_ITERATIONS 40

//ph_required: uniform vec3 relativeEyePosition;

struct HandheldSample {
    Light light;
    vec3 dir;
    float luminanace;
    bool valid;
};

void handheld_sample_init(inout HandheldSample smple, Light light, bool right_hand) {
    if (!any(notEqual(light.color, vec3(0.0f)))) {
        smple.valid = false;
        smple.luminanace = 0.0f;

        return;
    }

    smple.light = light;
    if (frag_is_hand) {
        smple.valid = true;
        return;
    }

    mat4 direction_transformation_matrix = inverse(gbufferProjection * gbufferModelView);

    vec4 direction_vert_out = direction_transformation_matrix * vec4(right_hand ? 1.0f : -1.0f, -1.0f, 0.0f, 1.0f);
    direction_vert_out.w = 1.0f / direction_vert_out.w;
    direction_vert_out.xyz *= direction_vert_out.w;

    smple.light.position = direction_vert_out.xyz + rt_camera_position - relativeEyePosition;

    vec3 to_light_dir = frag_rt_pos - smple.light.position;
    vec3 color = ph_compute_attenuation(
        smple.light,
        -to_light_dir,
        frag_rt_pos,
        smple.light.position,
        frag_geo_normal,
        frag_is_hand ? frag_geo_normal : frag_tex_normal
    );

    if (all(equal(light.color, vec3(0.0f)))) {
        smple.valid = false;
        smple.luminanace = 0.0f;

        return;
    }

    smple.dir = to_light_dir;
    smple.luminanace = ph_luminance(smple.light.color);
    smple.light.color = color;

//    #ifdef PH_HANDHELD_LIGHT_PULSE_MODIFIER_DISABLED
//    smple.light.color *= (ph_h(frameCounter / 300.0f) * 0.1f + ph_h(frameCounter / 100.0f) * 0.05f) + 0.4f;
//    #else
//    smple.light.color *= modify_handheld_pulse();
//    #endif

    smple.valid = smple.luminanace >= 0.0001f;
}

bool handheld_sample_trace(in HandheldSample smple, out vec3 tint_color, out float light_transmittance) {
    if (!smple.valid || frag_is_hand) return false;

    RayIterator ray;

    ray_iter_begin(ray, smple.light.position, smple.dir);
    ray.iterations = PH_HANDHELD_RAY_ITERATIONS;

    RayResult result = missed_ray_result();

    vec4 running_tint_color = vec4(0.0f);
    light_transmittance = 1.0f;

    float frag_dist = dot(smple.dir, smple.dir);
    float ray_dist = 0.0f;

    while (ray_iter_has_next(ray)) {
        result = ray_iter_next(ray);

        vec3 result_pos = ray_result_position(result);
        ray_dist = dot(result_pos - smple.light.position, result_pos - smple.light.position);

        if (ray_result_is_transparent(result)) {
            if (ray_dist > frag_dist) break;

            VoxelData voxel_data = ray_result_voxel_data(result);
            vec4 albedo = voxel_data_albedo(voxel_data);

            light_transmittance *= 1.0f - albedo.a;
            ray_iter_apply_transparency(running_tint_color, albedo);
            ray_iter_skip_block(ray);

            continue;
        }

        break;
    }

    tint_color = running_tint_color.a == 0.0f ? vec3(1.0f) : running_tint_color.rgb;
    return !ray_result_is_hit(result) || (ray_dist - frag_dist) > -0.1f;
}

vec3 handheld_sample_compute_color(inout HandheldSample smple, bool hit, vec3 tint_color, float light_transmittance) {
    if (!smple.valid) return vec3(0.0f);
    if (frag_is_hand) return smple.light.color;

    if (!hit) return vec3(0.0f);

    return max(
        smple.light.color * tint_color * light_transmittance,
        0.0f
    );
}

void sample_handheld(out vec3 color) {
    color = vec3(0.0f);
    if (!main_hand_has_light && !off_hand_has_light) return;

    HandheldSample main_hand;
    HandheldSample off_hand;

    if (main_hand_has_light) {
        handheld_sample_init(main_hand, get_main_hand_light(), !left_handed);
    } else main_hand.valid = false;

    if (off_hand_has_light) {
        handheld_sample_init(off_hand, get_off_hand_light(), left_handed);
    } else off_hand.valid = false;

    vec3 tint_color = vec3(1.0f);
    float light_transmittance = 1.0f;

    #ifdef PH_SEPARATE_HANDHELD_RAYS
        bool hit = handheld_sample_trace(main_hand, tint_color, light_transmittance);
        color+= handheld_sample_compute_color(main_hand, hit, tint_color, light_transmittance);

        hit = handheld_sample_trace(off_hand, tint_color, light_transmittance);
        color+= handheld_sample_compute_color(off_hand, hit, tint_color, light_transmittance);
    #else
       bool hit = handheld_sample_trace(off_hand.luminanace > main_hand.luminanace ? off_hand : main_hand, tint_color, light_transmittance);

        color+= handheld_sample_compute_color(main_hand, hit, tint_color, light_transmittance);
        color+= handheld_sample_compute_color(off_hand, hit, tint_color, light_transmittance);
    #endif
}
