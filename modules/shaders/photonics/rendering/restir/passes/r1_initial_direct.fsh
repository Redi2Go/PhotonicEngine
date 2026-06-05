#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;

void main() {
    if (!prepare_frag(0)) discard;

    DirectReservoir reservoir = direct_reservoir_empty();
    float sample_weight = 0.0f;

    if (light_list_size > 0) {
        for (int i = 0; i < PH_RESTIR_INITIAL_SAMPLES; i++) {
            DirectSample smple = direct_sample_random(frag_rnd_state);
            float weight = direct_sample_get_weight(
                smple,
                frag_rt_pos,
                frag_geo_normal,
                frag_is_hand ? frag_geo_normal : frag_tex_normal
            );

            if (direct_reservoir_update(reservoir, smple, weight, 1.0f))
                sample_weight = weight;
        }
    }

    direct_reservoir_finalize_weight(reservoir, sample_weight);
    direct_reservoir_encode(reservoir, di_reservoir_0);
}
