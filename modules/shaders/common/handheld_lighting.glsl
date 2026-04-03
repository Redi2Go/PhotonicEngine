#include "/photonics/modifiers/handheld_light_pulse_modifier.glsl"

struct HandheldSample {
    Light light;
    vec3 dir;
    float luminanace;
    bool valid;
};

void handheld_sample_init(inout HandheldSample smple, Light light, bool right_hand) {
    if (!any(notEqual(light.color, vec3(0f)))) {
        smple.valid = false;
        smple.luminanace = 0f;

        return;
    }

    smple.light = light;

    vec4 direction_vert_out = direction_transformation_matrix_in * vec4(right_hand ? 1.0f : -1.0f, -1.0f, 0.0f, 1.0f);
    direction_vert_out.w = 1.0f / direction_vert_out.w;
    direction_vert_out.xyz *= direction_vert_out.w;

    smple.light.position = direction_vert_out.xyz + rt_camera_position - relativeEyePosition;

    vec3 to_light_dir = smple.light.position - rt_pos;
    vec3 color = ph_compute_attenuation(
        smple.light,
        to_light_dir,
        rt_pos,
        smple.light.position,
        block_normal,
        normal
    );

    if (all(equal(light.color, vec3(0f)))) {
        smple.valid = false;
        smple.luminanace = 0f;

        return;
    }

    smple.dir = normalize(-to_light_dir);
    smple.luminanace = ph_luminance(smple.light.color);
    smple.light.color = color;

    #ifdef PH_HANDHELD_LIGHT_PULSE_MODIFIER_DISABLED
    smple.light.color *= (ph_h(frameCounter / 300.0f) * 0.1f + ph_h(frameCounter / 100.0f) * 0.05f) + 0.4f;
    #else
    smple.light.color *= modify_handheld_pulse();
    #endif

    smple.valid = true;
}

void handheld_sample_trace(in HandheldSample smple) {
    if (!smple.valid || ph_frag_is_hand) return;

    ray.origin = smple.light.position;
    ray.direction = smple.dir;

    RAY_ITERATION_COUNT = 20;
    trace_ray(ray, true);
    RAY_ITERATION_COUNT = 100;
}

vec3 handheld_sample_compute_color(inout HandheldSample smple) {
    if (!smple.valid) return vec3(0f);
    if (!ph_frag_is_hand) {
        if (!ray.result_hit) return vec3(0f);

        float hand_to_base_distance = distance(ray.origin, rt_pos);
        float hand_to_result_distance = distance(ray.origin, ray.result_position);
        smple.light.color *= clamp(30.0f * (hand_to_result_distance - hand_to_base_distance + 0.05f), 0.0f, 1.0f);
    }

    return max(smple.light.color * result_tint_color, 0f);
}

void sample_handheld(out vec4 color) {
    color = vec4(0f);
    if (!main_hand_has_light && !off_hand_has_light) return;

    HandheldSample main_hand;
    HandheldSample off_hand;

    if (main_hand_has_light) {
        handheld_sample_init(main_hand, load_main_hand_light(), !left_handed);
    } else main_hand.valid = false;

    if (off_hand_has_light) {
        handheld_sample_init(off_hand, load_off_hand_light(), left_handed);
    } else off_hand.valid = false;

    #ifdef PH_SEPARATE_HANDHELD_RAYS
        handheld_sample_trace(main_hand);
        color+= vec4(handheld_sample_compute_color(main_hand), 1f);

        handheld_sample_trace(off_hand);
        color+= vec4(handheld_sample_compute_color(off_hand), 1f);
    #else
       handheld_sample_trace(off_hand.luminanace > main_hand.luminanace ? off_hand : main_hand);

        color+= vec4(handheld_sample_compute_color(main_hand), 1f);
        color+= vec4(handheld_sample_compute_color(off_hand), 1f);
    #endif
}