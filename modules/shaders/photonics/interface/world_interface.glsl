//HEAD

//TODO: DEPRECATED; REMOVE IN FUTURE RELEASE
#include "/photonics/deprecated/shader_interface.glsl"

//bool is_in_world();

bool is_hand_at() {
    return texelFetch(depthtex1, ivec2(gl_FragCoord.xy), 0).x < 0.56;
}

vec3 load_player_position() {
    return load_world_position() - cameraPosition;
}

void load_fragment_data(
    out vec3 geometry_normal,
    out vec3 texture_normal
) {
    vec3 temp;

    load_fragment_variables(temp, temp, geometry_normal, texture_normal);
}

//vec2 get_taa_jitter();
