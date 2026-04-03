#file "/program/d4_deferred_shading.fsh"

#replace "uniform sampler2D noisetex;"
#define USE_RT

// Hack to get around replacing the moved frame counter
uniform int isEyeInWater, frameCounter;
uniform sampler2D radiosity_indirect;

#include "/photonics/photonics.glsl"

uniform sampler2D noisetex;
#endreplace

#replace "uniform int frameCounter;"
#endreplace

#replace "uniform int isEyeInWater;"
#endreplace

#replace "#include "/include/lighting/specular_lighting.glsl""
#endreplace

#replace "void main() {"
#include "/include/lighting/specular_lighting.glsl"

void main() {
#endreplace

#replace "fragment_color = get_diffuse_lighting("
fragment_color = get_diffuse_lighting(
#ifdef USE_RT
   !is_lod,
#endif
#endreplace