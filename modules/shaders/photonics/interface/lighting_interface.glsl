//HEAD

//TODO: DEPRECATED; REMOVE IN FUTURE RELEASE
#define NO_SHADOW_MAPPING

vec3 get_sun_direction() {
    return sun_direction;
}

vec3 get_sun_color() {
    return indirect_light_color;
}

vec3 get_sky_color() {
    return indirect_light_color;
}

bool is_in_shadow_at(vec3 player_pos, vec3 geo_normal) {
    return false;
}
