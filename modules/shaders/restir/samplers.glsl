uniform sampler2D radiosity_position;
uniform sampler2D radiosity_normal;
uniform sampler2D radiosity_reservoirs;
uniform sampler2D radiosity_lighting;
uniform sampler2D radiosity_lighting_variance;
uniform sampler2D radiosity_lighting_samples;
uniform sampler2D radiosity_handheld;

uniform sampler2D prev_radiosity_position;
uniform sampler2D prev_radiosity_normal;
uniform sampler2D prev_radiosity_reservoirs;
uniform sampler2D prev_radiosity_lighting;
uniform sampler2D prev_radiosity_lighting_variance;
uniform sampler2D prev_radiosity_lighting_samples;
uniform sampler2D prev_radiosity_handheld;

#if PH_RESTIR_DENOISER_PASSES != 0
uniform sampler2D denoise_color;
uniform sampler2D denoise_variance;

uniform sampler2D prev_denoise_color;
uniform sampler2D prev_denoise_variance;
#endif

vec3 sample_photonics_direct(vec2 tex_coord) {
    #if PH_RESTIR_DENOISER_PASSES != 0
    return texture(denoise_color, tex_coord).rgb;
    #else
    vec4 lighting = texture2D(radiosity_lighting, tex_coord);
    return (lighting.rgb / max(lighting.a, 1f));
    #endif
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
    #ifdef PH_ENABLE_HANDHELD_LIGHT
    return texture(radiosity_handheld, tex_coord).rgb;
    #else
    return vec3(0f);
    #endif
}