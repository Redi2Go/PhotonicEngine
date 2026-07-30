uniform sampler2D radiosity_position;
uniform sampler2D radiosity_normal;
uniform sampler2D radiosity_direct;
uniform sampler2D radiosity_direct_soft;
uniform sampler2D radiosity_handheld;

uniform sampler2D prev_radiosity_position;
uniform sampler2D prev_radiosity_normal;
uniform sampler2D prev_radiosity_reservoirs;
uniform sampler2D prev_radiosity_direct;
uniform sampler2D prev_radiosity_direct_soft;
uniform sampler2D prev_radiosity_handheld;

vec3 sample_photonics_direct(vec2 tex_coord) {
    vec4 direct_soft = texture(radiosity_direct_soft, tex_coord);

    return (direct_soft.rgb / max(direct_soft.a, 1f)) +
        texture(radiosity_direct, tex_coord).rgb;
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
    #ifdef PH_ENABLE_HANDHELD_LIGHT
    return texture(radiosity_handheld, tex_coord).rgb;
    #else
    return vec3(0f);
    #endif
}