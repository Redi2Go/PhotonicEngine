#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;

void main() {
    setup_frag_data(31);
    if (!frag_is_in_world) discard;

    IndirectReservoir reservoir = indirect_reservoir_empty();
    indirect_reservoir_load_previous(reservoir, frag_tex_coord, false);

    indirect_reservoir_validate_visiblity(reservoir, frag_rt_pos);
    indirect_reservoir_encode(reservoir, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
}
