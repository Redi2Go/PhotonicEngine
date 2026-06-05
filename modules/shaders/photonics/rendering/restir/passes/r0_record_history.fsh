#version 430

layout(location = 0) out vec3 position_history_out;
layout(location = 1) out vec4 normal_history_out;

#include "/photonics/rendering/common.glsl"

void main() {
    if (!prepare_frag(0)) {
        position_history_out = vec3(0.0f);
        normal_history_out = vec4(-1000.0f);
    } else {
        position_history_out = frag_player_pos;
        normal_history_out = vec4(
            ph_encode_normal(frag_geo_normal),
            ph_encode_normal(frag_tex_normal)
        );
    }
}