#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

#include "/photonics/modifiers/restir_gi_modifier.glsl"
#include "/photonics/interface/lighting_interface.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;

layout(location = RESTIR_LIGHTING_OUT) out vec4 lighting;

void main() {
    lighting = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    if (!prepare_frag(0)) return;

    // INDIRECT LIGHTING

    IndirectReservoir indirect_reservoir = indirect_reservoir_empty();
    indirect_reservoir_load(indirect_reservoir, frag_tex_coord);

    lighting.rgb = indirect_reservoir_get_final_color(indirect_reservoir);

//#ifndef PH_RESTIR_GI_MODIFIER_DISABLED
//    modify_restir_gi(lighting.rgb);
//#endif

//    indirect_reservoir_encode(indirect_reservoir, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);

    // DIRECT LIGHTING

    DirectReservoir direct_reservoir = direct_reservoir_empty();
    direct_reservoir_load(direct_reservoir, frag_tex_coord);
    lighting.rgb += direct_reservoir_get_final_color(
        direct_reservoir,
        frag_rt_pos,
        frag_geo_normal,
        frag_is_hand ? frag_geo_normal : frag_tex_normal
    );

    direct_reservoir_encode(direct_reservoir, di_reservoir_0);
}
