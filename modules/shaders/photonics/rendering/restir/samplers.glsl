#if PH_RESTIR_DENOISER_PASSES != 0
//ph_required: uniform usampler2D denoise_result;
#else
//ph_required: uniform sampler2D restir_lighting;
#endif

#if defined PH_ENABLE_HANDHELD_LIGHT
//ph_required: uniform sampler2D other_handheld;
#endif

vec3 sample_photonics_direct(vec2 tex_coord) {
#if PH_RESTIR_DENOISER_PASSES != 0
    uvec2 smple = texture(denoise_result, tex_coord).xy;

    return vec3(
        unpackHalf2x16(smple.x),
        unpackHalf2x16(smple.y).x
    );
#else
    vec4 lighting = texture(restir_lighting, tex_coord);
    return (lighting.rgb / max(lighting.a, 1.0f));
#endif
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
#if defined PH_ENABLE_HANDHELD_LIGHT
    return texture(other_handheld, tex_coord).rgb;
#else
    return vec3(0.0f);
#endif
}
