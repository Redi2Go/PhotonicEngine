#ifndef PH_SAMPLERS_INCLUDE
#define PH_SAMPLERS_INCLUDE

#if PH_LIGHTING_MODE == 0
#include "/photonics/off/samplers.glsl"
#elif PH_LIGHTING_MODE == 1
#include "/photonics/basic/samplers.glsl"
#elif PH_LIGHTING_MODE == 2
#include "/photonics/restir/samplers.glsl"
#endif

#endif