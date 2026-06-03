#version 430

#define PH_LIGHTING_PASS

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/indirect_lighting.glsl"
#include "/photonics/modifiers/restir_gi_modifier.glsl"

layout(location = 3) out vec3 lighting_out;

uniform sampler2D restir_lighting;

void main() {
    if (!prepare_frag(0)) return;

    lighting_out = texelFetch(restir_lighting, frag_tex_coord, 0).rgb;

    vec3 indirect_lighting;
    sample_indirect(indirect_lighting, frag_rt_pos, frag_geo_normal, frag_rnd_state);

#ifndef PH_RESTIR_GI_MODIFIER_DISABLED
    modify_restir_gi(indirect_lighting);
#endif

    lighting_out+= indirect_lighting;
}
