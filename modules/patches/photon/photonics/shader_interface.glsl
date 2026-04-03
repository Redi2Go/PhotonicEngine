#file "/photonics/shader_interface.glsl"

#replace "// HEAD"
#include "/include/global.glsl"

uniform sampler2D colortex1;// albedo, flat_normal
uniform sampler2D colortex2;// detailed_normal
uniform sampler2D colortex4; // sky
uniform sampler2D depthtex1; // depth no transulscents

uniform vec3 light_dir;
uniform float near;
uniform float far;
uniform float worldTime;

uniform vec2 taa_offset;
uniform mat4 gbufferProjection;
uniform mat4 gbufferModelView;
uniform vec2 view_pixel_size;

#include "/include/utility/space_conversion.glsl"
#include "/include/utility/encoding.glsl"

#if defined OVERWORLD && defined SH_SKYLIGHT && defined PH_RESTIR_COMBINED_GI
#include "/include/utility/spherical_harmonics.glsl"
#endif

#define load_tex_coord gl_FragCoord.xy * view_pixel_size
#endreplace

#replace "vec3 load_world_position();"
vec3 photon_indirect_color = vec3(0f);

vec3 load_world_position() {
    vec2 tex_coord = load_tex_coord;
    vec3 screen_pos = vec3(tex_coord.xy * rcp(taau_render_scale), texture(depthtex1, tex_coord).r);

    vec3 view_pos = screen_to_view_space(screen_pos, true);
    vec3 scene_pos = view_to_scene_space(view_pos);

    return scene_pos + cameraPosition;
}
#endreplace

#replace "vec2 get_taa_jitter() { return vec2(0f); }"
vec2 get_taa_jitter() {
#ifdef TAA
#ifdef TAAU
    return taa_offset * rcp(taau_render_scale);
#else
    return taa_offset * 0.66;
#endif
#else
    return vec2(0f);
#endif
}
#endreplace

#replace "void load_fragment_variables(out vec3 albedo, out vec3 world_pos, out vec3 world_normal, out vec3 world_normal_mapped);"
void load_fragment_variables(out vec3 albedo, out vec3 world_pos, out vec3 world_normal, out vec3 world_normal_mapped) {
    vec2 tex_coord = load_tex_coord;
    vec4 gbuffer_data_0 = texture(colortex1, tex_coord);
    albedo.rg = unpack_unorm_2x8(gbuffer_data_0.x);
    albedo.b = unpack_unorm_2x8(gbuffer_data_0.y).x;

    world_normal = decode_unit_vector(unpack_unorm_2x8(gbuffer_data_0.z));

    #if defined NORMAL_MAPPING
    vec4 gbuffer_data_1 = texture(colortex2, tex_coord);
    world_normal_mapped = decode_unit_vector(gbuffer_data_1.xy);
    #else
    world_normal_mapped = world_normal;
    #endif

#if defined PH_LIGHTING_PASS
    #if defined OVERWORLD && defined SH_SKYLIGHT && defined PH_RESTIR_COMBINED_GI
    vec3 sky_sh[9] = vec3[9](
        texelFetch(colortex4, ivec2(191, 2), 0).rgb,
        texelFetch(colortex4, ivec2(191, 3), 0).rgb,
        texelFetch(colortex4, ivec2(191, 4), 0).rgb,
        texelFetch(colortex4, ivec2(191, 5), 0).rgb,
        texelFetch(colortex4, ivec2(191, 6), 0).rgb,
        texelFetch(colortex4, ivec2(191, 7), 0).rgb,
        texelFetch(colortex4, ivec2(191, 8), 0).rgb,
        texelFetch(colortex4, ivec2(191, 9), 0).rgb,
        texelFetch(colortex4, ivec2(191, 10), 0).rgb
    );

    photon_indirect_color = sh_evaluate_irradiance(sky_sh, vec3(0f), 1f);
    #else
    photon_indirect_color = mix(texelFetch(colortex4, ivec2(191, 1), 0).rgb, vec3(1f), 0.5);
    #endif
#endif


    world_pos = load_world_position() - 0.01f * world_normal;
}
#endreplace

#replace "vec3 sun_direction;"
vec3 sun_direction = light_dir;
#endreplace

#replace "vec3 indirect_light_color;"
#define indirect_light_color photon_indirect_color
#endif
#endreplace

#replace "vec3 get_sky_color(ivec2 gBufferLoc, vec3 worldPos, vec3 newNormal);"
vec3 get_sky_color(ivec2 gBufferLoc, vec3 worldPos, vec3 newNormal) {
    return vec3(1f);
}
#endreplace

#replace "bool is_in_world() { return texelFetch(depthtex0, ivec2(gl_FragCoord.xy), 0).x <= 0.99999f; }"
bool is_in_world() {
    return texelFetch(depthtex1, ivec2(gl_FragCoord.xy), 0).x <= 0.99999f;
}
#endreplace