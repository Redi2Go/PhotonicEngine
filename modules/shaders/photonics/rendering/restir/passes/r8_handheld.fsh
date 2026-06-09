#version 430

#define FRAG_USE_RT_POS
#define FRAG_USE_GEO_NORMAL
#define FRAG_USE_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/handheld_lighting.glsl"

layout(location = 0) out vec3 handheld_out;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) {
        handheld_out = vec3(0.0f);
        return;
    }

    sample_handheld(handheld_out);
}
