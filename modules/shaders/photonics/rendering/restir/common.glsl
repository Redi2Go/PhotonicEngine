#ifndef PH_SHARED_INCLUDE
#define PH_SHARED_INCLUDE

#define MINIMUM_RESERVOIR_WEIGHT 0.000001f

//TODO Rename restir combined gi
#if defined PH_ENABLE_GI && defined PH_RESTIR_COMBINED_GI
#define PH_ENABLE_RESTIR_GI
#endif

#include "/photonics/utility/projection.glsl"
#include "/photonics/utility/normal_encoding.glsl"
#include "/photonics/utility/color.glsl"

#endif
