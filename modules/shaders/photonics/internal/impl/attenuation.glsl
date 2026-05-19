#include "/photonics/modifiers/attenuation_modifier.glsl"

#ifdef PH_ATTENUATION_MODIFIER_DISABLED
vec3 ph_compute_attenuation(
    Light light,
    vec3 to_light,
    vec3 sample_pos,
    vec3 source_pos,
    vec3 geometry_normal,
    vec3 texture_normal
) {
    // Fix light on handheld light sources being weird
    //if (light.index < 0 && ph_is_hand()) return light.color;

    float distance_squared = dot(to_light, to_light);
    float light_dist_inv = inversesqrt(distance_squared);
    vec3 light_dir = to_light * light_dist_inv;
    if (dot(light_dir, geometry_normal) < 0.01f) return vec3(0f);

    float att = 1f / (distance_squared * light.falloff * light.attenuation.y + light.attenuation.x);
    att *= clamp(dot(texture_normal, light_dir), 0f, 1f);

    return att * light.color;
}
#else
#define ph_compute_attenuation(light, to_light, sample_pos, source_pos, geometry_normal, texture_normal) modify_attenuation(light, to_light, sample_pos, source_pos, geometry_normal, texture_normal)
#endif