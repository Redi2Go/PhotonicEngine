#ifndef PH_SAMPLERS_INCLUDE
#define PH_SAMPLERS_INCLUDE

#if defined PH_OFF_ACTIVE
#include "/photonics/rendering/off/samplers.glsl"
#elif defined PH_SHARP_ACTIVE
#include "/photonics/rendering/sharp/samplers.glsl"
#elif defined PH_RESTIR_ACTIVE
#include "/photonics/rendering/restir/samplers.glsl"
#endif

#endif
