#version 430

#define FRAG_USE_RT_POS
#define FRAG_USE_GEO_NORMAL
#define FRAG_USE_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

#include "/photonics/modifiers/restir_gi_modifier.glsl"

#if defined PH_ENABLE_BLOCKLIGHT
layout(location = DIRECT_RESERVOIR_0) out vec3 di_reservoir_0;
#endif

#if defined PH_ENABLE_RESTIR_GI
layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;
#endif

layout(location = RESTIR_LIGHTING_OUT) out vec4 lighting;

void main() {
    lighting = vec4(0.0f, 0.0f, 0.0f, 1.0f);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

#if defined PH_ENABLE_RESTIR_GI
    // INDIRECT LIGHTING

    IndirectReservoir indirect_reservoir = indirect_reservoir_empty();
    indirect_reservoir_load(indirect_reservoir, frag_tex_coord);

    lighting.rgb = indirect_reservoir_get_final_color(indirect_reservoir);

#ifndef PH_RESTIR_GI_MODIFIER_DISABLED
    modify_restir_gi(lighting.rgb);
#endif

    indirect_reservoir_encode(indirect_reservoir, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
#endif


//#if defined PH_ENABLE_BLOCKLIGHT
//    // DIRECT LIGHTING
//
//    DirectReservoir direct_reservoir = direct_reservoir_empty();
//    direct_reservoir_load(direct_reservoir, frag_tex_coord);
//    lighting.rgb += direct_reservoir_get_final_color(
//        direct_reservoir,
//        frag_rt_pos,
//        frag_geo_normal,
//        frag_is_hand ? frag_geo_normal : frag_tex_normal
//    );
//
//    direct_reservoir_encode(direct_reservoir, di_reservoir_0);
//#endif
}
