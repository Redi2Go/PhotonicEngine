#version 430

#define USE_FRAG_GEO_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"
#include "/photonics/rendering/restir/neighbor/reservoir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"

//ph_required: uniform sampler2D depthtex0;
//ph_Required: uniform float near, far;

// Store the samples in lighting as this value hasn't been initialized yet
layout(location = RESTIR_LIGHTING_OUT) out vec4 neighbor_samples;

#define BATCH_SIZE 5

void prepare_neighbor_batch(out vec3[BATCH_SIZE] batch) {
    for (int i = 0; i < BATCH_SIZE; i++) {
        batch[i].x = frag_rnd_state;

        ivec2 sample_texel = neighbor_next_sample(frag_rnd_state);

        // .y for geo normal, .z for tex normal
        batch[i].y = texelFetch(ph_frag_data1, sample_texel, 0).y;
        batch[i].z = texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(sample_texel), 0).r;
    }
}

void main() {
    setup_frag_data(31);
    if (!frag_is_in_world) discard;

    #define N0 frag_geo_normal
    float D0 = svgf_linearize_depth(texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(frag_tex_coord), 0).r);

    NeighborReservoir reservoir = neighbor_reservoir_empty();

    vec3 batch[BATCH_SIZE];
    const float phi_depth = 0.5f;

    for (int b = 0; b < 6; b++) {
        prepare_neighbor_batch(batch);

        for (int i = 0; i < BATCH_SIZE; i++) {
            vec3 Ni = ph_decode_normal(unpackSnorm2x16(floatBitsToUint(batch[i].y)));
            float Di = svgf_linearize_depth(batch[i].z);

            float wN = svgf_normal_edge_stopping_weight(N0, Ni);
            float wP = svgf_depth_edge_stopping_weight(D0, Di, phi_depth);

            neighbor_reservoir_feed_sample(reservoir, frag_rnd_state, batch[i].x, wN * wP);
        }
    }

    neighbor_reservoir_encode_samples(reservoir, neighbor_samples);
}
