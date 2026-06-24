#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

#include "/photonics/rendering/indirect_lighting.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;

    IndirectReservoir reservoir = indirect_reservoir_empty();

    reservoir.smple.rnd_state = frag_rnd_state;
    reservoir.smple.visible_normal = frag_geo_normal;

    sample_indirect(
            reservoir.smple.color,
            frag_rt_pos,
            frag_geo_normal,
            frag_is_hand ? frag_geo_normal : frag_tex_normal,
            frag_rnd_state,

            reservoir.smple.hit_position,
            reservoir.smple.hit_normal
    );

    if (reservoir.smple.hit_position.x == -1.0f) {
        reservoir.smple.traced_sky = true;
    } else {
        reservoir.smple.hit_distance = distance(frag_rt_pos, reservoir.smple.hit_position);
        reservoir.smple.hit_position-= rt_camera_position;
    }

    reservoir.weight = ph_luminance(reservoir.smple.color);
    reservoir.total_samples = 1.0f;

    indirect_reservoir_finalize_weight(reservoir, reservoir.weight);
    indirect_reservoir_encode(reservoir, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
}
