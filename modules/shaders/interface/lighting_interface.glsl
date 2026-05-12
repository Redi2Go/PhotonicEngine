// TO BE REMOVED FOR 0.6
#include "/photonics/interface/shader_interface.glsl"

vec3 get_sun_direction() {
    return sun_direction;
}

vec3 get_sun_color() {
    return indirect_light_color * 8;
}

vec3 get_sky_color(ivec2 tex_coord, vec3 world_pos) {
    return indirect_light_color * 2;
}