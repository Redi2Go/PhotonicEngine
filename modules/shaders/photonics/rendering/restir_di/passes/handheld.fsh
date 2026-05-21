#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/handheld_lighting.glsl"

layout(location = 6) out vec3 handheld_out;

void main() {
    if (!prepare_frag(0)) return;

    sample_handheld(handheld_out);
}