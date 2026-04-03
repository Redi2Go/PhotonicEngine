#file "/program/gbuffers_all_translucent.fsh"


#replace "#include "/include/fog/simple_fog.glsl""
#if defined PROGRAM_GBUFFERS_WATER && defined WORLD_SPACE_REFLECTIONS
#define USE_RT

uniform sampler2D radiosity_indirect;
#include "/photonics/photonics.glsl"
#endif

#ifdef CLOUD_SHADOWS
#include "/include/lighting/cloud_shadows.glsl"
#endif

#include "/include/fog/simple_fog.glsl"
#endreplace

#replace "get_diffuse_lighting("
    get_diffuse_lighting(
#ifdef USE_RT
        false,
#endif
#endreplace