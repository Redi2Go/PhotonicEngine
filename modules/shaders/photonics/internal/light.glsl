#ifndef PH_LIGHT_INCLUDE
#define PH_LIGHT_INCLUDE

#include "/photonics/uniforms.glsl"

struct Light {
    int index;
    int blockId;
    vec3 position;
    vec3 color;
    float intensity;
    vec2 attenuation;
    float falloff;
    float block_radius;
};

#include "/photonics/modifiers/light_modifier.glsl"
#include "/photonics/internal/impl/attenuation.glsl"

Light new_light_from_vec4(
    vec4 position_full,
    vec4 color_full,
    vec4 attenuation_full,
    int index
) {
    vec3 light_pos = position_full.xyz;
    vec3 rt_pos = light_pos - light_list_offset;

    #ifndef PH_LIGHT_MODIFIER_DISABLED
    Light light = Light(
        index,
        floatBitsToInt(position_full.w),
        rt_pos,
        color_full.xyz,
        color_full.w,
        attenuation_full.xy,
        attenuation_full.z,
        attenuation_full.w
    );

    modify_light(light, rt_pos + world_offset);

    return light;
    #else
    return Light(
        index,
        floatBitsToInt(position_full.w),
        rt_pos,
        color_full.xyz,
        color_full.w,
        attenuation_full.xy,
        attenuation_full.z,
        attenuation_full.w
    );
    #endif
}

vec3 light_sample_at(
    Light light,
    vec3 sample_pos,
    vec3 source_pos,
    vec3 geometry_normal,
    vec3 texture_normal
) {
    return ph_compute_attenuation(
        light,
        sample_pos - source_pos,
        sample_pos,
        source_pos,
        geometry_normal,
        texture_normal
    );
}

#endif