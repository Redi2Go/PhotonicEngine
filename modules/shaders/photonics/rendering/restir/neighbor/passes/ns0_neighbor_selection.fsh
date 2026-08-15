#version 430

#define frag_tex_coord ivec2(gl_FragCoord.xy)

//ph_required: uniform int frameCounter;

#include "/photonics/utility/normal_encoding.glsl"

#include "/photonics/rendering/frag/fast_data.glsl"
#include "/photonics/rendering/restir/neighbor/reservoir.glsl"
#include "/photonics/rendering/restir/svgf/common.glsl"


// Store the samples in lighting as this value hasn't been initialized yet
layout(location = NEIGHBOR_RESERVOIR_OUT) out vec4 neighbor_samples;

void main() {
    FastFrag center_data = fast_frag_fetch(frag_tex_coord);
    if (!fast_frag_in_world(center_data)) discard;

    float D0 = center_data.depth;
    vec3  N0 = fast_frag_tex_normal(center_data);

    uint rnd_state = ph_new_rand_state(gl_FragCoord.xy, frameCounter, 4532789);

    NeighborReservoir reservoir;
    neighbor_reservoir_init(reservoir);

    for (int i = 0; i < 30; i++) {
        // Values of 0 will be skipped by spatial reuse, but realistically
        // its so rare its not worth thinking about
        float smple = uintBitsToFloat(rnd_state);
        ivec2 sample_texel = neighbor_next_sample(rnd_state);
        FastFrag sample_data  = fast_frag_fetch(sample_texel);

        float Di = sample_data.depth;
        vec3  Ni = fast_frag_tex_normal(sample_data);

        if (!isinf(Di)) {
            const float phi_depth = 0.1f;
            float wP = svgf_depth_edge_stopping_weight(D0, Di, phi_depth);
            float wN = svgf_normal_edge_stopping_weight(N0, Ni);

            neighbor_reservoir_feed_sample(reservoir, rnd_state, smple, wN * wP);
        }
    }

    neighbor_reservoir_encode_samples(reservoir, neighbor_samples);
}
