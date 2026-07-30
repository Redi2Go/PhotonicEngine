#file "/include/lighting/diffuse_lighting.glsl"

#replace "vec3 get_diffuse_lighting("
vec3 get_diffuse_lighting(
#ifdef USE_RT
    bool sample_rt,
#endif
#endreplace

#replace "vec3 bounced = 0.033 * (1.0 - shadows) * (1.0 - 0.1 * max0(normal.y)) *"
#if defined USE_RT && defined WORLD_OVERWORLD

vec3 gi_color;
vec3 bounced;
if (sample_rt) {
    #if !defined PH_RESTIR_COMBINED_GI
    gi_color = texture2D(radiosity_indirect, uv).xyz;
    bounced = gi_color * 0.364f * BOUNCED_LIGHT_I;
    #else
    bounced = vec3(0f);
    #endif
} else {
    gi_color = vec3(0f);
    bounced = 0.033 * (1.0 - shadows) * (1.0 - 0.1 * max0(normal.y)) *
        pow1d5(ao + eps) * pow4(light_levels.y);

    bounced*= BOUNCED_LIGHT_I; // hack to avoid getting replaced
}
#else
vec3 bounced = 0.033 * (1.0 - shadows) * (1.0 - 0.1 * max0(normal.y)) *
#endreplace

#replace "pow1d5(ao + eps) * pow4(light_levels.y) * BOUNCED_LIGHT_I;"
pow1d5(ao + eps) * pow4(light_levels.y) * BOUNCED_LIGHT_I;
#endif
#endreplace

#replace "lighting += skylight * get_skylight_falloff(light_levels.y);"
vec3 skylight_color = skylight * get_skylight_falloff(light_levels.y);

#if defined USE_RT && defined WORLD_OVERWORLD
if (sample_rt) {
    #if !defined PH_RESTIR_COMBINED_GI
    skylight_color-= dot(gi_color, luminance_weights_rec709);
    skylight_color = max(skylight_color, 0f) * 0.4;
    #else
    skylight_color = vec3(0f);
    #endif
}
#endif

lighting+= skylight_color;
#endreplace

#replace "// Blocklight"
#if defined USE_RT
if (sample_rt) {
    lighting+= sample_photonics_direct(uv) * 3.4f * BLOCKLIGHT_I;
    lighting+= sample_photonics_handheld(uv) * 3.4f * HANDHELD_LIGHTING_INTENSITY;
} else {
#endif

#endreplace

#replace "lighting += material.emission * emission_scale;"
#if defined USE_RT
}
#endif

lighting += material.emission * emission_scale;
#endreplace