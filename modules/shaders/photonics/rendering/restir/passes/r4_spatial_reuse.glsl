#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out vec4 gi_reservoir_1;
layout(location = INDIRECT_RESERVOIR_2) out vec4 gi_reservoir_2;

void main() {
    if (!prepare_frag(0)) discard;

    float samples = texelFetch(restir_lighting_samples, frag_tex_coord, 0).r;
    bool is_low_samples = samples < min(PH_RESTIR_ACCUMULATION_FRAMES - 1, 4);

#if REUSE_ITERATION > 0
    if (!is_low_samples) discard;
#endif

    float direct_sample_weight = 0.0f;
    DirectReservoir direct_result = direct_reservoir_empty();
    DirectReservoir temp_direct = direct_reservoir_empty();

    float indirect_sample_weight = 0.0f;
    IndirectReservoir indirect_result = indirect_reservoir_empty();
    IndirectReservoir temp_indirect = indirect_reservoir_empty();

    // load current reservoir
    direct_reservoir_load(temp_direct, frag_tex_coord);
    direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);

    indirect_reservoir_load(temp_indirect, frag_tex_coord);
    indirect_reservoir_merge(indirect_result, temp_indirect, 1.0f, indirect_sample_weight);

    const float ph_spatial_reuse_radius = PH_RESTIR_SPATIAL_REUSE_RADIUS * PH_RENDER_SCALE * (REUSE_ITERATION + 1);

    int direct_reuse_samples = is_low_samples ? PH_RESTIR_SPATIAL_REUSE_SAMPLES * 3 : PH_RESTIR_SPATIAL_REUSE_SAMPLES;

    for (int i = 0; i < direct_reuse_samples; i++) {
        vec2 offset = 2.0 * vec2(ph_rand_next_float(frag_rnd_state), ph_rand_next_float(frag_rnd_state)) - 1.0f;
        ivec2 sample_texel = ivec2(frag_tex_coord + offset * ph_spatial_reuse_radius);

        vec3 sample_player_pos = texelFetch(restir_position_history, sample_texel, 0).xyz;
        vec3 d = sample_player_pos - frag_player_pos;
        if (dot(d, d) >= 2f) continue;


        vec4 sample_normals = texelFetch(restir_normal_history, sample_texel, 0);
        vec3 sample_geo_normal = ph_decode_normal(sample_normals.xy);
        if (dot(sample_geo_normal, frag_geo_normal) < 0.99f) continue;

        if (direct_reservoir_load(temp_direct, sample_texel))
            direct_reservoir_merge(direct_result, temp_direct, direct_sample_weight);

        if (indirect_reservoir_load(temp_indirect, sample_texel)) {
            temp_indirect.total_samples = 1.0f;

            indirect_reservoir_merge(
                indirect_result,
                temp_indirect,
                1.0f,
                indirect_sample_weight
            );
        }
    }

    direct_reservoir_clamp_samples(direct_result);

    direct_reservoir_finalize_weight(direct_result, direct_sample_weight);
    direct_reservoir_encode(direct_result, di_reservoir_0);


    indirect_reservoir_clamp_samples(indirect_result);

    indirect_reservoir_finalize_weight(indirect_result, indirect_sample_weight);
    indirect_reservoir_encode(indirect_result, gi_reservoir_0, gi_reservoir_1, gi_reservoir_2);
}
