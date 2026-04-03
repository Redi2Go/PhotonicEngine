#file "/photonics/shader_interface.glsl"

#replace "// HEAD"
#define shadow2D texture
#define texture2D texture

#define FSH

#undef DISTANT_HORIZONS
#undef VOXY

flat in vec3 sunVecWorld;
flat in vec3 sunVec;
flat in vec3 upVec;

uniform ivec2 eyeBrightnessSmooth;
uniform float timeAngle, timeBrightness;
uniform float far, near;
uniform float nightVision;
uniform int isEyeInWater;

uniform sampler2D colortex6, colortex10, colortex11;

#include "/lib/settings.glsl"

float GetLuminance(vec3 color) {
    return dot(color,vec3(0.299, 0.587, 0.114));
}

uniform int worldTime;
uniform int moonPhase;
uniform float cloudHeight;
uniform int bedrockLevel;

#if MC_VERSION >= 12109
uniform float endFlashIntensity;
#endif

#ifdef WORLD_TIME_ANIMATION
float time = float(worldTime) * 0.05 * ANIMATION_SPEED;
#else
float time = frameTimeCounter * ANIMATION_SPEED;
#endif

float eBS = eyeBrightnessSmooth.y / 240.0;
vec3 shadowVec = (sunVecWorld.y < 0.0f) ? -sunVecWorld : sunVecWorld;
float sunVisibility  = clamp(dot( sunVec, upVec) * 10.0 + 0.5, 0.0, 1.0);
float sunSkyVisibility = sunVisibility;

float moonVisibility = clamp(dot(-sunVec, upVec) * 10.0 + 0.5, 0.0, 1.0);
float sunVisibilitySaturated = clamp(sunVisibility, 0.0f, 1.0f);

vec3 lightVec = sunVec * ((timeAngle < 0.5325 || timeAngle > 0.9675) ? 1.0 : -1.0);

#include "/lib/color/blocklightColor.glsl"
#include "/lib/color/dimensionColor.glsl"

#ifndef OVERWORLD
#include "/lib/color/lightColor.glsl"
#endif

#include "/lib/util/jitter.glsl"
#include "/lib/util/spaceConversion.glsl"
#include "/lib/lighting/forwardLighting.glsl"
#include "/lib/util/dither.glsl"
#include "/lib/color/skyColor.glsl"
#include "/lib/atmospherics/weatherDensity.glsl"
#include "/lib/atmospherics/clouds.glsl"
#include "/lib/atmospherics/sky.glsl"

#include "/lib/util/encode.glsl"

#define PH_BSL_UNIFORM_HACK

#endreplace

#replace "void load_fragment_variables(out vec3 albedo, out vec3 world_pos, out vec3 world_normal, out vec3 world_normal_mapped);"
void load_fragment_variables(out vec3 albedo, out vec3 world_pos, out vec3 world_normal, out vec3 world_normal_mapped) {
    vec2 texCoord = gl_FragCoord.xy / vec2(viewWidth, viewHeight);

    albedo = texture(colortex10, texCoord).xyz;

    vec4 buffer3 = texture(colortex6, texCoord);
    world_normal_mapped = normalize((gbufferModelViewInverse * vec4(DecodeNormal(buffer3.xy), 0.0f)).xyz);

    world_normal = normalize((gbufferModelViewInverse * vec4(2.0f * texture(colortex11, texCoord).xyz - 1.0f, 0.0f)).xyz);
    world_pos = load_world_position() - 0.01f * world_normal;
}
#endreplace

#replace "vec3 load_world_position();"
vec2 get_taa_offset() {
    #ifdef TAA
    vec2 view = 1.0 / vec2(viewWidth, viewHeight);

    #if TAA_MODE == 0
    vec2 jitterOffsets8[8] = vec2[8](
        vec2( 0.125,-0.375),
        vec2(-0.125, 0.375),
        vec2( 0.625, 0.125),
        vec2( 0.375,-0.625),
        vec2(-0.625, 0.625),
        vec2(-0.875,-0.125),
        vec2( 0.375,-0.875),
        vec2( 0.875, 0.875)
    );
    return jitterOffsets8[int(framemod8)] * view;
    #else
    vec2 jitterOffsets2[2] = vec2[2](
        vec2( 1.0,  0.0),
        vec2( 0.0,  1.0)
    );
    return jitterOffsets2[int(framemod2)] * view;
    #endif
    #endif

    return vec2(0.0);
}

vec3 load_world_position() {
    vec2 texCoord = gl_FragCoord.xy / vec2(viewWidth, viewHeight);
    vec3 screenPos = vec3(texCoord.xy, texture(depthtex0, texCoord).r);
    vec3 ndc = 2.0f * screenPos - 1.0f;
    ndc.xy -= get_taa_offset();

    vec4 viewPos = gbufferProjectionInverse * vec4(ndc, 1.0f);
    viewPos.xyz /= viewPos.w;
    vec3 worldPos = (gbufferModelViewInverse * vec4(viewPos.xyz, 1.0f)).xyz;

    return worldPos + cameraPosition;
}
#endreplace

#replace "vec3 sun_direction;"
vec3 sun_direction = mat3(gbufferModelViewInverse) * lightVec; // TODO: use lightVecWorld????
#endreplace

#replace "vec3 indirect_light_color;"
vec3 indirect_light_color = 0.3f * (sunVisibilitySaturated) * mix(lightCol, vec3(1.0f), 0.4f);
#endreplace

#replace "vec3 get_sky_color(ivec2 gBufferLoc, vec3 worldPos, vec3 newNormal);"
vec3 get_sky_color(ivec2 gBufferLoc, vec3 worldPos, vec3 newNormal) {
    newNormal = mat3(transpose(gbufferModelViewInverse)) * newNormal;
    vec3 skyReflection = vec3(0.0);

    vec3 screenPos = vec3(gBufferLoc / vec2(viewWidth, viewHeight), gl_FragCoord.z);
    #ifdef TAA
    vec3 viewPos = ToNDC(vec3(TAAJitter(screenPos.xy, -0.5), screenPos.z));
    #else
    vec3 viewPos = ToNDC(screenPos);
    #endif

    float dither = Bayer8(gl_FragCoord.xy);

    #ifdef OVERWORLD
    vec3 skyRefPos = reflect(normalize(viewPos), newNormal);
    skyReflection = GetSkyColor(skyRefPos, true);

    #if defined(AURORA) && AURORA > 1
    skyReflection += DrawAurora(skyRefPos * 100.0, dither, 12);
    #endif

    #if CLOUDS == 1
    vec4 cloud = DrawCloudSkybox(skyRefPos * 100.0, 1.0, dither, lightCol, ambientCol, true);
    skyReflection = mix(skyReflection, cloud.rgb, cloud.a);
    #endif
    #if CLOUDS == 2
    vec3 cameraPos = GetReflectedCameraPos(worldPos - cameraPosition, newNormal);
    float cloudViewLength = 0.0;

    vec4 cloud = DrawCloudVolumetric(skyRefPos * 8192.0, cameraPos, 1.0, dither, lightCol, ambientCol, cloudViewLength, true);
    skyReflection = mix(skyReflection, cloud.rgb, cloud.a);
    #endif

    #ifdef CLASSIC_EXPOSURE
    skyReflection *= 4.0 - 3.0 * eBS;
    #endif

    float waterSkyOcclusion = 1.2f; // lightmap.y;
    #if REFLECTION_SKY_FALLOFF > 1
    waterSkyOcclusion = clamp(1.0 - (1.0 - waterSkyOcclusion) * REFLECTION_SKY_FALLOFF, 0.0, 1.0);
    #endif
    waterSkyOcclusion *= waterSkyOcclusion;
    skyReflection *= waterSkyOcclusion;
    #endif

    #ifdef NETHER
    skyReflection = netherCol.rgb * 0.04;
    #endif

    #ifdef END
    skyReflection = endCol.rgb * 0.01;
    #endif

    skyReflection *= clamp(1.0 - isEyeInWater, 0.0, 1.0);

    return skyReflection;
}
#endreplace