#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;
layout(location = RESTIR_LIGHTING_OUT) out vec4 lighting;

void main() {
    setup_frag_data(0);

#if defined PH_ENABLE_RESTIR_GI
    if (!frag_is_in_world) discard;
    lighting = texelFetch(restir_lighting, frag_tex_coord, 0);
#else
    lighting = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    if (!frag_is_in_world) return;
#endif

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
