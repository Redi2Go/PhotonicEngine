#include "/photonics/uniforms.glsl"
#include "/photonics/interface/world_interface.glsl"
#include "/photonics/utility/random.glsl"
#include "/photonics/utility/normal_encoding.glsl"

uniform int frameCounter;

ivec2 ph_tex_coord = ivec2(0);
uint ph_rnd_state;

vec3 ph_rt_pos = vec3(0.0f);

vec3 ph_geo_normal = vec3(0.0f);
vec3 ph_tex_normal = vec3(0.0f);
//bool bad_angle = false;
//bool ph_frag_is_hand = false;

bool prepare_frag(int rnd_seed) {
    ph_tex_coord = ivec2(gl_FragCoord.xy);
    if (!is_in_world_at(ph_tex_coord)) return false;

    ph_rnd_state = new_rand_state(gl_FragCoord.xy, frameCounter, 0);

    ph_rt_pos = get_player_position(ph_tex_coord) + rt_camera_position;
    get_fragment_data(ph_tex_coord, ph_geo_normal, ph_tex_normal);

    // Attempts to correct bias from depth

    vec3 voxel_pos = ph_rt_pos * 16.0f;
    uint normal_index = ph_encode_voxel_normal(ph_geo_normal);
    float normal_pos = round(voxel_pos[normal_index >> 1]);

    vec3 view_dir =  normalize(ph_rt_pos - rt_camera_position);
    float view_distance = distance(ph_rt_pos, rt_camera_position);

    voxel_pos += (view_dir * (view_distance * 0.03));
    voxel_pos[normal_index >> 1] = normal_pos;

    const float rcp_16 = 1.0f / 16.0f;
    ph_rt_pos = voxel_pos * rcp_16;

    return true;
}