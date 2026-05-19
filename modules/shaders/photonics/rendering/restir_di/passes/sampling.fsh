#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir_di/restir.glsl"

layout(location = 2) out vec4 reservoir_out;

layout(location = 0) out vec3 position_history_out;
layout(location = 1) out vec4 normal_history_out;

void main() {
    if (!prepare_frag(0)) return;

    if (light_list_size != 0) {
        Reservoir reservoir = reservoir_new();
        reservoir_init(reservoir, frag_rt_pos);

        vec3 temp_tint;
        float temp_illum;

        light_sample_trace_hit(reservoir.light, temp_tint, temp_illum, false);
        reservoir_compute_weight(reservoir);

        Reservoir previous_reservoir = reservoir_new();
        if (reservoir_reproject(previous_reservoir)) {
            Reservoir temporal_reservoir = reservoir_new();

            if (reservoir_is_valid(reservoir)) {
                reservoir_update(
                    temporal_reservoir,
                    reservoir.light,
                    reservoir.light.weight * reservoir.weight * reservoir.samples,
                    reservoir.samples
                );
            }

            previous_reservoir.samples = min(20.0f * reservoir.samples, previous_reservoir.samples);

            reservoir_update(
                temporal_reservoir,
                previous_reservoir.light,
                previous_reservoir.light.weight * previous_reservoir.weight * previous_reservoir.samples,
                previous_reservoir.samples
            );

            reservoir = temporal_reservoir;
            reservoir_compute_weight(reservoir);
        }

        reservoir_out = reservoir_encode(reservoir);
    }

    position_history_out = frag_player_pos;
    normal_history_out = vec4(
        ph_encode_normal(frag_geo_normal),
        ph_encode_normal(frag_tex_normal)
    );
}