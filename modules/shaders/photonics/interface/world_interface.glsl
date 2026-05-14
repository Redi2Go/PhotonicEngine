//HEAD

bool is_in_world(vec2 tex_coord);

vec3 get_player_position(vec2 tex_coord);

void get_fragment_data(
    vec2 tex_coord,
    out vec3 geometry_normal,
    out vec3 texture_normal
);

vec2 get_taa_jitter();