#include "/photonics/rendering/frag/world_interface.glsl"
#include "/photonics/utility/normal_encoding.glsl"
#include "/photonics/rendering/frag/frag_data.glsl"
#include "/photonics/utility/random.glsl"

#define frag_tex_coord ivec2(gl_FragCoord.xy)
#define PH_VIEW_SIZE (vec2(viewWidth, viewHeight) * PH_RENDER_SCALE)

uint frag_rnd_state = 0u;

FragData _frag_data;

#if defined FRAG_USE_PLAYER_POS
vec3 frag_player_pos;
#else
#define frag_player_pos frag_data_player_pos(_frag_data)
#endif

#if defined FRAG_USE_RT_POS
vec3 frag_rt_pos;
#else
#define frag_rt_pos frag_data_rt_pos(_frag_data)
#endif

#if defined FRAG_USE_GEO_NORMAL
vec3 frag_geo_normal;
#else
#define frag_geo_normal frag_data_geo_normal(_frag_data)
#endif

#if defined FRAG_USE_TEX_NORMAL
vec3 frag_tex_normal;
#else
#define frag_tex_normal frag_data_tex_normal(_frag_data)
#endif

#define frag_is_in_world frag_data_is_in_world(_frag_data)
#define frag_is_bad_angle frag_data_is_bad_angle(_frag_data)
#define frag_is_hand frag_data_is_hand(_frag_data)

void setup_frag_data(int rnd_seed) {
    frag_rnd_state = ph_new_rand_state(gl_FragCoord.xy, frameCounter, 0);
    frag_data_load(_frag_data, frag_tex_coord);

#if defined FRAG_USE_PLAYER_POS
    frag_player_pos = frag_data_player_pos(_frag_data);
#endif

#if defined FRAG_USE_RT_POS
    frag_rt_pos = frag_data_rt_pos(_frag_data);
#endif

#if defined FRAG_USE_GEO_NORMAL
    frag_geo_normal = frag_data_geo_normal(_frag_data);
#endif

#if defined FRAG_USE_TEX_NORMAL
    frag_tex_normal = frag_data_tex_normal(_frag_data);
#endif
}
