#file "/world0/gbuffers_voxels.fsh" "/world1/gbuffers_voxels.fsh" "/world-1/gbuffers_voxels.fsh"
#create

// Compatibility is required, otherwise Iris will use the Sodium-Core patches (which does not include stuff like gl_NormalMatrix
#version 430 compatibility

#define OVERWORLD
#define FSH

#include "/program/gbuffers_voxels.glsl"
