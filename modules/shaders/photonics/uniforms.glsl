#ifndef PH_UNIFORMS_INCLUDE
#define PH_UNIFORMS_INCLUDE

// tracing uniforms

uniform vec3 rt_camera_position;
uniform vec3 world_offset;
uniform vec3 world_max_voxel;
uniform vec3 world_min_voxel;


// light uniforms

uniform vec3 light_list_offset;
uniform int light_list_size;

uniform bool left_handed;

uniform bool off_hand_has_light;
uniform mat4 ph_off_hand_light;

uniform bool main_hand_has_light;
uniform mat4 ph_main_hand_light;

#endif