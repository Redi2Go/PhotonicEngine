#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir_di/restir.glsl"

layout(location = 0) out vec4 reservoir_out;

void main() {
    if (!prepare_frag(0)) return;

    Reservoir reservoir = reservoir_new();
    reservoir_init(reservoir, ph_rt_pos);

    vec3 temp_tint;
    float temp_illum;

    light_sample_trace_hit(reservoir.light, temp_tint, temp_illum, false);
    reservoir_compute_weight(reservoir);

    reservoir_out = reservoir_encode(reservoir);
}