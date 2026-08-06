#include "/photonics/utility/random.glsl"

#define NEIGHBOR_RESERVOIR_OUT 0

uniform sampler2D neighbor_reservoir;

struct NeighborReservoir {
    int size;
    int min_index;

    float[PH_RESTIR_SPATIAL_REUSE_SAMPLES] weights;
    float[PH_RESTIR_SPATIAL_REUSE_SAMPLES] samples;
};

ivec2 neighbor_next_sample(inout uint rnd) {
    const float reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE;

    vec2 offset = 2.0 * vec2(ph_rand_next_float(rnd), ph_rand_next_float(rnd)) - 1.0f;
    return ivec2(frag_tex_coord + offset * reuse_radius);
}

void neighbor_reservoir_init(out NeighborReservoir reservoir) {
    reservoir.size = 0;
    reservoir.min_index = 0;

    // Dont like GLSL and internet wont tell me if this is possible any other way so
#if PH_RESTIR_SPATIAL_REUSE_SAMPLES == 4
    #define array_init float[](0.0f, 0.0f, 0.0f, 0.0f)
#elif PH_RESTIR_SPATIAL_REUSE_SAMPLES == 3
    #define array_init float[](0.0f, 0.0f, 0.0f)
#elif PH_RESTIR_SPATIAL_REUSE_SAMPLES == 2
    #define array_init float[](0.0f, 0.0f)
#elif PH_RESTIR_SPATIAL_REUSE_SAMPLES == 1
    #define array_init float[](0.0f)
#endif

    reservoir.weights = array_init;
    reservoir.samples = array_init;
}

void neighbor_reservoir_feed_sample(
        inout NeighborReservoir reservoir,
        inout uint rnd_state,
        float smple,
        float weight
) {
    float key = pow(ph_rand_next_float(rnd_state), 1.0f / weight);

    if (reservoir.size < PH_RESTIR_SPATIAL_REUSE_SAMPLES) {
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
    for (int i = 1; i < PH_RESTIR_SPATIAL_REUSE_SAMPLES; i++)
        reservoir.min_index = reservoir.weights[i] < reservoir.weights[reservoir.min_index] ? i : reservoir.min_index;
}

void neighbor_reservoir_encode_samples(NeighborReservoir reservoir, out vec4 result) {
    result = vec4(0.0f);

    for (int i = 0; i < PH_RESTIR_SPATIAL_REUSE_SAMPLES; i++) {
        result[i] = reservoir.samples[i];
    }
}

void neighbor_load_samples(ivec2 texel, out uvec4 samples) {
    samples = floatBitsToUint(texelFetch(neighbor_reservoir, texel, 0));
}
