#ifndef PH_SAMPLERS_INCLUDE
#define PH_SAMPLERS_INCLUDE

#if PH_LIGHTING_MODE == 0
#include "/photonics/rendering/off/samplers.glsl"
#elif PH_LIGHTING_MODE == 1
#include "/photonics/rendering/basic/samplers.glsl"
#elif PH_LIGHTING_MODE == 2
#include "/photonics/rendering/restir_di/samplers.glsl"
#endif

#endif