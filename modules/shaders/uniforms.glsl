#ifndef PH_UNIFORMS_INCLUDE
#define PH_UNIFORMS_INCLUDE

// tracing uniforms

uniform vec3 rt_camera_position;
uniform vec3 world_offset;
uniform vec3 world_max_voxel;
uniform vec3 world_min_voxel;


// light uniforms

uniform vec3 light_list_offset;
uniform int ph_light_count;

#endif