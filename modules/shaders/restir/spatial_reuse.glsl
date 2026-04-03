/*
    -- INPUT VARIABLES --
*/
in vec4 direction_vert_out;

/*
    -- OUTPUT VARIABLES --
*/
layout(location = 2) out vec4 reservoir_frag_out;

#define PH_USE_RNG_SEED
#define PH_RNG_SEED REUSE_ITERATION

#include "/photonics/common/header.glsl"
#include "/photonics/restir/restir.glsl"

void main() {
#ifdef PH_ENABLE_BLOCKLIGHT
    if (!is_in_world() || ph_light_count == 0) return;

    load_fragment_variables(albedo, world_pos, block_normal, normal);
    rt_pos = world_pos - world_offset;
    bad_angle = is_bad_angle(world_pos, block_normal);
    ph_frag_is_hand = ph_is_hand();

    float samples = texelFetch(radiosity_lighting_samples, tex_coord, 0).r;

    #if REUSE_ITERATION > 0
    if (samples >= min(PH_RESTIR_ACCUMULATION_FRAMES - 1, 4)) {
        reservoir_frag_out = texelFetch(radiosity_reservoirs, tex_coord, 0);

        return;
    }
    #endif

    Reservoir reservoir = reservoir_new();
    reservoir_decode(
        reservoir,
        texelFetch(radiosity_reservoirs, tex_coord, 0),
        rt_pos,
        false
    );

    Reservoir temp_reservoir = reservoir_new();
    const float ph_spatial_reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE * (REUSE_ITERATION + 1);
    for (int i = 0; i < PH_RESTIR_SPATIAL_REUSE_SAMPLES; i++) {
        vec2 offset = 2.0 * vec2(rand_next_float(), rand_next_float()) - 1f;
        ivec2 uv = ivec2(tex_coord + offset * ph_spatial_reuse_radius);

        if (!reservoir_reuse(temp_reservoir, uv)) continue;

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

    reservoir_frag_out = reservoir_encode(reservoir);
#else
    return;
#endif
}