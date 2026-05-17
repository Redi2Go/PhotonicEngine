#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir_di/restir.glsl"

layout(location = 2) out vec4 reservoir_out;

void main() {
    if (!prepare_frag(REUSE_ITERATION)) return;

#if REUSE_ITERATION > 0
    float samples = texelFetch(restir_lighting_samples, frag_tex_coord, 0).r;
    if (samples >= min(PH_RESTIR_ACCUMULATION_FRAMES - 1, 4)) {
        reservoir_out = texelFetch(restir_reservoirs, frag_tex_coord, 0);

        return;
    }
#endif

    Reservoir reservoir = reservoir_new();
    reservoir_decode(
        reservoir,
        texelFetch(restir_reservoirs, frag_tex_coord, 0),
        frag_rt_pos,
        false
    );

    Reservoir temp_reservoir = reservoir_new();
    const float ph_spatial_reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE * (REUSE_ITERATION + 1);
    for (int i = 0; i < PH_RESTIR_SPATIAL_REUSE_SAMPLES; i++) {
        vec2 offset = 2.0 * vec2(rand_next_float(frag_rnd_state), rand_next_float(frag_rnd_state)) - 1f;
        ivec2 texel = ivec2(frag_tex_coord + offset * ph_spatial_reuse_radius);

        if (!reservoir_reuse(temp_reservoir, texel)) continue;

        reservoir_update(
            reservoir,
            temp_reservoir.light,
            temp_reservoir.light.weight * temp_reservoir.weight * temp_reservoir.samples,
            temp_reservoir.samples
        );
    }

    if (reservoir_is_valid(reservoir)) {
        reservoir_compute_weight(reservoir);
        reservoir_clamp_samples(reservoir, MAX_RESERVOIR_SAMPLES);
    }

    reservoir_out = reservoir_encode(reservoir);
}