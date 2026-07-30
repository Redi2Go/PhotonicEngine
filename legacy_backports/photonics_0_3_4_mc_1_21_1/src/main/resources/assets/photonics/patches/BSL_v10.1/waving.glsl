#file "/lib/vertex/waving.glsl"

// Disable block waving
#replace "vec3 WavingBlocks(vec3 position, int blockID, float istopv) {"
vec3 WavingBlocks(vec3 position, int blockID, float istopv) { return position;
#endreplace

