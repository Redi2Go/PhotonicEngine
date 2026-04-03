#file "/program/gbuffers_voxels.fsh"
#template "/program/gbuffers_all_solid.fsh"

#replace "#include "/include/global.glsl""
#include "/include/global.glsl"
#endreplace

#replace "in vec4 tint;"
in vec4 tint;
in vec3 block_normal;
#endreplace

#replace "#if TEXTURE_FORMAT == TEXTURE_FORMAT_LAB"
#include "/photonics/photonics.glsl"

#if TEXTURE_FORMAT == TEXTURE_FORMAT_LAB
#endreplace

// This check is not unnescary but we need to remove everything
// from base_color to '#if defined PROGRAM_GBUFFERS_ENTITIES' anyway
#replace "vec4 base_color = read_tex(gtexture) * tint;"
#if defined PHOTONICS_ENABLED
vec3 screen_pos = vec3(
gl_FragCoord.xy * view_pixel_size * rcp(taau_render_scale),
gl_FragCoord.z
);

vec3 view_pos = screen_to_view_space(screen_pos, true);
vec3 scene_pos = view_to_scene_space(view_pos);

vec3 world_pos = scene_pos + cameraPosition;
vec3 world_dir = normalize(scene_pos - gbufferModelViewInverse[3].xyz);

RayJob ray = RayJob(
world_pos - world_offset - 0.001f * block_normal, // Ray origin
world_dir, // Ray direction
vec3(0), vec3(0), vec3(0), false
);

ray_constraint = ivec3(ray.origin);
trace_ray(ray);

if (!ray.result_hit) discard;
if (ray.result_normal == vec3(0.0)) ray.result_normal = block_normal;

scene_pos = ray.result_position + world_offset - cameraPosition;
view_pos = scene_to_view_space(scene_pos);
screen_pos = view_to_screen_space(gbufferProjection, view_pos, false);

gl_FragDepth = screen_pos.z;

vec4 base_color = vec4(ray.result_color, 1f);
vec3 ph_normal = ray.result_normal;

#if defined SPECULAR_MAPPING
vec4 specular_map = vec4(0f);
#endif
#else
//--//

vec4 base_color = read_tex(gtexture) * tint;
#endreplace

#replace "#if defined PROGRAM_GBUFFERS_ENTITIES"
#endif

#if defined PROGRAM_GBUFFERS_ENTITIES
#endreplace

#replace "vec3 normal;"
#endreplace

#replace "float material_ao;"
#endreplace

#replace "decode_normal_map(normal_map, normal, material_ao);"
#endreplace

#replace "normal = tbn * normal;"
#endreplace

#replace "adjusted_light_levels *= mix(0.7, 1.0, material_ao);"
#endreplace

#replace "adjusted_light_levels *= get_directional_lightmaps(scene_pos, normal);"
#endreplace

#replace "#if defined NO_NORMAL"
#if defined PHOTONICS_ENABLED
#define flat_normal ph_normal
#define detailed_normal ph_normal
#elif defined NO_NORMAL
#endreplace