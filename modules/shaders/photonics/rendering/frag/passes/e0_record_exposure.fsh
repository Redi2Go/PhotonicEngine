#version 430

#include "/photonics/rendering/frag/world_interface.glsl"

layout(location = 0) out float exposure_out;

void main() {
#if !defined PH_EXPOSURE_ADJUSTMENT
    exposure_out = 1.0f;
#else
    exposure_out = get_exposure();
#endif
}
