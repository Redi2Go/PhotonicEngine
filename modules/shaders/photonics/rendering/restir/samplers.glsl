#if PH_RESTIR_DENOISER_PASSES > 0
//ph_required: uniform usampler2D denoise_result;
#else
//ph_required: uniform usampler2D diffuse_history;
#endif

#if defined PH_ENABLE_HANDHELD_LIGHT
//ph_required: uniform bool off_hand_has_light, main_hand_has_light;
//ph_required: uniform sampler2D handheld_diffuse;
#endif

uniform sampler2D di_output;

vec3 sample_photonics_direct(vec2 tex_coord) {
    return texture(di_output, tex_coord).rgb;

//#if PH_RESTIR_DENOISER_PASSES != 0
//    return uintBitsToFloat(texture(denoise_result, tex_coord).rgb);
//#else
//    uvec2 packed = texture(restir_lighting, tex_coord).xy;
//
//    return vec4(
//        unpackHalf2x16(packed.x),
//        unpackHalf2x16(packed.y)
//    ).rgb;
//#endif
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
#if defined PH_ENABLE_HANDHELD_LIGHT
    return (main_hand_has_light || off_hand_has_light) ? texture(handheld_diffuse, tex_coord).rgb : vec3(0.0f);
#else
    return vec3(0.0f);
#endif
}
