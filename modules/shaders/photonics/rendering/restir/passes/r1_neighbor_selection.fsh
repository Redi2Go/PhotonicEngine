#version 430

#define frag_tex_coord ivec2(gl_FragCoord.xy)

//ph_required: uniform sampler2D depthtex0;
//ph_required: uniform sampler2D prev_restir_lighting_variance;
//ph_required: uniform int frameCounter;
//ph_required: uniform float near, far;

#include "/photonics/rendering/restir/neighbor/reservoir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"
#include "/photonics/utility/normal_encoding.glsl"

// Store the samples in lighting as this value hasn't been initialized yet
layout(location = 2) out vec4 neighbor_samples;

void main() {
    vec2 center_data = texelFetch(restir_neighbor_data, frag_tex_coord, 0).xy;
    if (isinf(center_data.x)) discard;

    float D0 = center_data.x;
    vec3  N0 = unpack_normal(center_data.y);

    uint rnd_state = ph_new_rand_state(gl_FragCoord.xy, frameCounter, 4532789);

    NeighborReservoir reservoir;
    neighbor_reservoir_init(reservoir);

    for (int i = 0; i < 30; i++) {
        // Values of 0 will be skipped by spatial reuse, but realistically
        // its so rare its not worth thinking about
        float smple = uintBitsToFloat(rnd_state);
        ivec2 sample_texel = neighbor_next_sample(rnd_state);
        vec2  sample_data  = texelFetch(restir_neighbor_data, sample_texel, 0).xy;

        float Di = sample_data.x;
        vec3  Ni = ph_unpack_normal(sample_data.y);

        if (!isinf(Di)) {
            const float phi_depth = 0.5f;
            float wP = svgf_depth_edge_stopping_weight(D0, Di, phi_depth);
            float wN = svgf_normal_edge_stopping_weight(N0, Ni);

            neighbor_reservoir_feed_sample(reservoir, rnd_state, smple, wN * wP);
        }
    }

    neighbor_reservoir_encode_samples(reservoir, neighbor_samples);
}
