#version 430

#define USE_FRAG_GEO_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"
#include "/photonics/rendering/restir/neighbor/reservoir.glsl"
#include "/photonics/rendering/restir/svgf.glsl"

//ph_required: uniform sampler2D depthtex0;
//ph_Required: uniform float near, far;

// Store the samples in lighting as this value hasn't been initialized yet
layout(location = 1) out vec4 neighbor_samples;

void main() {
    setup_frag_data(31);
    if (!frag_is_in_world) discard;

    #define N0 frag_geo_normal
    float D0 = svgf_linearize_depth(texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(frag_tex_coord), 0).r);

    NeighborReservoir reservoir = neighbor_reservoir_empty();
    const float phi_depth = 0.5f;

    for (int i = 0; i < 30; i++) {
        float smple = uintBitsToFloat(frag_rnd_state);
        ivec2 sample_texel = neighbor_next_sample(frag_rnd_state);

        // .y for geo normal, .z for tex normal
        float PNi = texelFetch(ph_frag_data1, sample_texel, 0).y;

        float Di = svgf_linearize_depth(texelFetch(depthtex0, SVGF_DEPTH_MODIFIER(sample_texel), 0).r);
        float wP = svgf_depth_edge_stopping_weight(D0, Di, phi_depth);

        vec3  Ni = ph_decode_normal(unpackSnorm2x16(floatBitsToUint(PNi)));
        float wN = svgf_normal_edge_stopping_weight(N0, Ni);

        neighbor_reservoir_feed_sample(reservoir, frag_rnd_state, smple, wN * wP);
    }

    neighbor_reservoir_encode_samples(reservoir, neighbor_samples);
}
