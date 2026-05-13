#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir_di/restir.glsl"

uniform sampler2D restir_reservoirs;

layout(location = 1) out vec3 lighting_out;

uniform float viewWidth;
uniform float viewHeight;

void main() {
    if (!prepare_frag(0)) return;

    Reservoir reservoir = reservoir_new();
    reservoir_decode(
        reservoir,
        texelFetch(restir_reservoirs, ph_tex_coord, 0),
        ph_rt_pos,
        false
    );

    if (reservoir_is_valid(reservoir)) {
        vec3 tint_color;
        float light_transmittance;
        light_sample_trace_hit(reservoir.light, tint_color, light_transmittance, true);

        lighting_out = vec3(reservoir.light.color * tint_color * light_transmittance * reservoir.weight);
    } else {
        lighting_out = vec3(0f);
    }
}