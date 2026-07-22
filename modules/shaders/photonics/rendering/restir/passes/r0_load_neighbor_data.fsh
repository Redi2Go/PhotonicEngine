#version 430

//ph_required: uniform sampler2D depthtex0;
//ph_required: uniform sampler2D prev_restir_lighting_variance;
//ph_Required: uniform float near, far;

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/neighbor/reservoir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"

layout(location = NEIGHBOR_DATA_OUT) out vec2 neighbor_data;

void main() {
    const float infinity = intBitsToFloat(0x7f800000);
    float depth = texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(frag_tex_coord), 0).r;

    setup_frag_data(31);
    if (!frag_is_in_world) {
        neighbor_data = vec2(infinity);
        return;
    }

    neighbor_data.x = svgf_linearize_depth(depth);

    // .y for geo normal, .z for tex normal
    neighbor_data.y = uintBitsToFloat(_frag_data.data1.y);

}
