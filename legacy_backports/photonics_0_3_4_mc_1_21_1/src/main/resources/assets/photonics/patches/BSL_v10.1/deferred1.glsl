#file "/program/deferred1.glsl"

#replace "#ifdef FSH"
#ifdef FSH
#include "/photonics/ph_samplers.glsl"

uniform sampler2D colortex10;
uniform sampler2D colortex11;
uniform int ph_light_count;
uniform bool main_hand_has_light;
uniform vec4 ph_main_hand_light1;

#ifndef PH_RESTIR_COMBINED_GI
uniform sampler2D colortex12;
#endif
#endreplace

#replace "vec4 color = texture2D(colortex0, texCoord);"
vec4 photonicsBase = texture2D(colortex0, texCoord);
vec3 photonicsAlbedo = texture2D(colortex10, texCoord).rgb;
vec3 photonicsDirect = sample_photonics_direct(texCoord) * photonicsAlbedo;
vec3 photonicsHandheld = sample_photonics_handheld(texCoord) * photonicsAlbedo;
vec3 photonicsIndirect = vec3(0.0);
#ifndef PH_RESTIR_COMBINED_GI
photonicsIndirect = texture2D(colortex12, texCoord).rgb * photonicsAlbedo;
#endif

vec4 color = photonicsBase;
#if PHOTONICS_DEBUG_VIEW == 1
color = photonicsBase;
#elif PHOTONICS_DEBUG_VIEW == 2
color = vec4(photonicsAlbedo, 1.0);
#elif PHOTONICS_DEBUG_VIEW == 3
color = vec4(texture2D(colortex11, texCoord).rgb, 1.0);
#elif PHOTONICS_DEBUG_VIEW == 4
color = vec4(photonicsDirect, 1.0);
#elif PHOTONICS_DEBUG_VIEW == 5
color = vec4(photonicsIndirect, 1.0);
#elif PHOTONICS_DEBUG_VIEW == 6
color = vec4(photonicsHandheld, 1.0);
#elif PHOTONICS_DEBUG_VIEW == 7
color = vec4(
    ph_light_count > 0 ? 1.0 : 0.0,
    main_hand_has_light ? 1.0 : 0.0,
    dot(abs(ph_main_hand_light1.rgb), vec3(1.0)) > 0.000001 ? 1.0 : 0.0,
    1.0
);
#elif PHOTONICS_DEBUG_VIEW == 8
vec4 photonicsReservoirState = texture2D(radiosity_reservoirs, texCoord);
vec4 photonicsNormalState = texture2D(radiosity_normal, texCoord);
vec3 photonicsRawLighting = texture2D(radiosity_lighting, texCoord).rgb;
color = vec4(
    dot(abs(photonicsNormalState), vec4(1.0)) > 0.000001 ? 1.0 : 0.0,
    photonicsReservoirState.w > 0.0 ? 1.0 : 0.0,
    dot(abs(photonicsRawLighting), vec3(1.0)) > 0.000001 ? 1.0 : 0.0,
    1.0
);
#elif PHOTONICS_DEBUG_VIEW == 9
color = vec4(texture2D(radiosity_lighting, texCoord).rgb, 1.0);
#else
color.rgb += (
    #ifndef PH_RESTIR_COMBINED_GI
    photonicsIndirect +
    #endif
    photonicsHandheld +
    photonicsDirect
);
#endif
#endreplace
