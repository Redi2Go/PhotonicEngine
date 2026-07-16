#include "/photonics/utility/random.glsl"

#define NEIGHBOR_SAMPLES 4

struct NeighborReservoir {
    int size;
    int min_index;

    vec4 weights;
    vec4 samples;
};

NeighborReservoir neighbor_reservoir_empty() {
    return NeighborReservoir(0, 0, vec4(0.0f), vec4(0.0f));
}

ivec2 neighbor_next_sample(inout uint rnd) {
    const float reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE;

    vec2 offset = 2.0 * vec2(ph_rand_next_float(rnd), ph_rand_next_float(rnd)) - 1.0f;
    return ivec2(frag_tex_coord + offset * reuse_radius);
}

void neighbor_reservoir_feed_sample(
        inout NeighborReservoir reservoir,
        inout uint rnd_state,
        float smple,
        float weight
) {
    float key = pow(ph_rand_next_float(rnd_state), 1.0f / weight);

    if (reservoir.size < NEIGHBOR_SAMPLES) {
        reservoir.weights[reservoir.size] = key;
        reservoir.samples[reservoir.size++] = smple;
    } else {
        float threshold = reservoir.weights[reservoir.min_index];

        if (key > threshold) {
            reservoir.weights[reservoir.min_index] = key;
            reservoir.samples[reservoir.min_index] = smple;
        }
    }

    reservoir.min_index = 0;
    for (int i = 1; i < NEIGHBOR_SAMPLES; i++)
        reservoir.min_index = reservoir.weights[i] < reservoir.weights[reservoir.min_index] ? i : reservoir.min_index;
}

void neighbor_reservoir_encode_samples(NeighborReservoir reservoir, out vec4 result) {
    result = reservoir.samples;
}

void neighbor_load_samples(ivec2 texel, out uvec4 samples) {
    samples = floatBitsToUint(texelFetch(prev_restir_lighting_variance, texel, 0));
}
