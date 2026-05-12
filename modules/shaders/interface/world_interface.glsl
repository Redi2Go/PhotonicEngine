// TO BE REMOVED FOR 0.6
#include "/photonics/interface/shader_interface.glsl"

bool is_in_world(ivec2 tex_coord) {
    return is_in_world();
}

vec3 get_world_position(ivec2 tex_coord) {
    return load_world_position();
}

void get_fragment_data(
    ivec2 tex_coord,
    out vec3 albedo,
    out vec3 player_pos,
    out vec3 geometry_normal,
    out vec3 texture_normal
) {
    load_fragment_variables(
        albedo,
        player_pos,
        geometry_normal,
        texture_normal
    );

    player_pos -= world_camera_position;
}

vec2 get_taa_jitter();