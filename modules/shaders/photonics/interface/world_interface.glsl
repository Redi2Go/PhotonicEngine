//HEAD

bool is_in_world();

vec3 load_player_position();

void load_fragment_data(
    out vec3 geometry_normal,
    out vec3 texture_normal
);

vec2 get_taa_jitter();