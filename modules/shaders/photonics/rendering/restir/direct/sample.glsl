#include "/photonics/light_list.glsl"
#include "/photonics/tracing.glsl"

#include "/photonics/utility/random.glsl"

struct DirectSample {
    int light_index; // The index of the sampled light, will be -1 when empty
    uint packed_offset;
};

DirectSample direct_sample_empty() {
    return DirectSample(-1, 0u);
}

DirectSample direct_sample_random(inout uint rnd_state) {
    return DirectSample(ph_rand_next_int(rnd_state, 0, light_list_size), 0u);
}

vec3 direct_sample_get_offset(DirectSample smple) {
    return unpackSnormR11G11B10(smple.packed_offset) * ph_light_jitter_radius;
}

bool direct_sample_is_empty(DirectSample smple) {
    return smple.light_index == -1;
}

float direct_sample_weight(vec3 color) {
    const float min_weight = 0.0001f;
    float weight = ph_luminance(color);

    return weight < min_weight ? 0.0f : weight;
}

Light direct_sample_get_light(DirectSample smple) {
    if (direct_sample_is_empty(smple))
        return new_invalid_light();

    Light light = light_list_get(int(smple.light_index));
    light.position+= direct_sample_get_offset(smple);

    return  light;
}

vec3 direct_sample_get_color(
    DirectSample smple,
    Light light,
    vec3 sample_pos,
    vec3 geo_normal,
    vec3 tex_normal
) {
    if (!light_is_valid(light))
        return vec3(0.0f);

    return light_sample_at(
        light,
        sample_pos,
        light.position,
        geo_normal,
        tex_normal
    ) * light_list_size;
}

float direct_sample_init_weight(
        inout DirectSample smple,
        vec3 sample_pos,
        vec3 geo_normal,
        vec3 tex_normal,
        inout uint rnd_state
) {
    if (direct_sample_is_empty(smple))
        return 0.0f;

    Light light = light_list_get(int(smple.light_index));

#if defined PH_RESTIR_SOFT_SHADOWS
    const vec3 offset = ph_rand_sample_position(rnd_state, light.position, sample_pos);
#else
    const vec3 offset = vec3(0.0f);
#endif

    smple.packed_offset = packSnormR11G11B10(offset / ph_light_jitter_radius);

    light.position+= direct_sample_get_offset(smple);
    vec3 color = direct_sample_get_color(smple, light, sample_pos, geo_normal, tex_normal);

    return direct_sample_weight(color);
}

float direct_sample_get_weight(
    DirectSample smple,
    vec3 sample_pos,
    vec3 geo_normal,
    vec3 tex_normal
) {
    if (direct_sample_is_empty(smple))
        return 0.0f;

    Light light = direct_sample_get_light(smple);
    vec3 color = direct_sample_get_color(smple, light, sample_pos, geo_normal, tex_normal);

    return direct_sample_weight(color);
}

bool direct_sample_reproject(inout DirectSample smple) {
    if (smple.light_index < 0) return false;

    smple.light_index = light_list_map_index(smple.light_index);
    if (smple.light_index < 0 || smple.light_index >= light_list_size) {
        smple.light_index = -1;
        return false;
    }

    return true;
}
