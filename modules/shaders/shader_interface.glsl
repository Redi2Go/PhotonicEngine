// HEAD

/*
    -- PATCH OVERWRITES --
*/
vec3 load_world_position();
void load_fragment_variables(out vec3 albedo, out vec3 world_pos, out vec3 world_normal, out vec3 world_normal_mapped);

vec2 get_taa_jitter() { return vec2(0f); }

vec3 sun_direction;
vec3 indirect_light_color;
vec3 get_sky_color(ivec2 tex_coord, vec3 world_pos);

bool is_in_world() { return texelFetch(depthtex0, ivec2(gl_FragCoord.xy), 0).x <= 0.99999f; }