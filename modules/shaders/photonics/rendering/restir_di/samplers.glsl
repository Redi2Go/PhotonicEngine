#if PH_RESTIR_DENOISER_PASSES != 0
uniform sampler2D denoise_color;
#else
uniform sampler2D restir_lighting;
#endif

vec3 sample_photonics_direct(vec2 tex_coord) {
    #if PH_RESTIR_DENOISER_PASSES != 0
    return texture(denoise_color, tex_coord).rgb;
    #else
    vec4 lighting = texture2D(restir_lighting, tex_coord);
    return (lighting.rgb / max(lighting.a, 1.0f));
    #endif
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
    return vec3(0.0f);
}