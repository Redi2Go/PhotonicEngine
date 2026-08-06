#define SVGF_HISTORY_OUT 0
#define SVGF_FAST_HISTORY_OUT 1

uniform usampler2D diffuse_history;
uniform usampler2D prev_diffuse_history;
uniform sampler2D prev_fast_diffuse_history;

struct SampleHistory {
    vec4 lighting;
    vec4 variance;
};

SampleHistory sample_history_empty() {
    return SampleHistory(vec4(0.0f), vec4(0.0f));
}

SampleHistory sample_history_invalid() {
    const float nan = intBitsToFloat(0x7fc00000);
    return SampleHistory(vec4(nan), vec4(nan));
}

bool sample_history_is_valid(SampleHistory history) {
    return all(not(isnan(history.lighting))) && all(not(isnan(history.variance)));
}

void sample_history_encode(SampleHistory history, out uvec4 value) {
#if PH_RESTIR_DENOISER_PASSES > 0
    value.x = packHalf2x16(history.lighting.xy);
    value.y = packHalf2x16(history.lighting.zw);

    value.z = packHalf2x16(history.variance.xy);
    value.w = packHalf2x16(history.variance.zw);
#else
    value = floatBitsToUint(history.lighting);
#endif
}

void sample_history_decode(out SampleHistory history, uvec4 value) {
#if PH_RESTIR_DENOISER_PASSES > 0
    history.lighting.xy = unpackHalf2x16(value.x);
    history.lighting.zw = unpackHalf2x16(value.y);

    history.variance.xy = unpackHalf2x16(value.z);
    history.variance.zw = unpackHalf2x16(value.w);
#else
    history.lighting = uintBitsToFloat(value);
    history.variance = vec4(0.f);
#endif
}

void sample_history_load(out SampleHistory history, ivec2 texel) {
    sample_history_decode(history, texelFetch(diffuse_history, texel, 0));
}

#if defined REPROJECT_PASS
void sample_history_reproject(out SampleHistory temporal_history, out vec4 fast_history) {
    temporal_history.lighting = vec4(0.0f);
    temporal_history.variance = vec4(0.0f);
    fast_history = vec4(0.0f);

    vec3 center = ph_reproject_player_pos(frag_player_pos, frag_is_hand, get_taa_jitter());
    center.xy *= PH_VIEW_SIZE;
    center.xy -= 0.5f;
    center.z = ph_linearize_depth(center.z);

    ivec2 texel = ivec2(center.xy);
    vec2 mixFactors = fract(center.xy);

    const ivec2[4] offsets = ivec2[](ivec2(0, 0), ivec2(1, 0), ivec2(0, 1), ivec2(1, 1));
    const vec2[4] weights = vec2[](vec2(1.0f, 1.0f), vec2(0.0f, 1.0f), vec2(1.0f, 0.0f), vec2(0.0f, 0.0f));

    float weight_sum = 0.0f;
    const float phi_depth = frag_is_bad_angle ? 0.25f : 0.07f;

    for (int i = 0; i < weights.length(); i++) {
        ivec2 p = texel + offsets[i];

        uvec4 temporal_sample = texelFetch(prev_diffuse_history, p, 0);
        vec4 fast_sample = texelFetch(prev_fast_diffuse_history, p, 0);

        FastFrag prevFrag = fast_frag_fetch_previous(p);

        vec2 mixWeights = abs(weights[i] - mixFactors);
        float weight = mixWeights.x * mixWeights.y;
        weight *= svgf_normal_edge_stopping_weight(frag_tex_normal, fast_frag_tex_normal(prevFrag));
        weight *= svgf_depth_edge_stopping_weight(center.z, prevFrag.depth, phi_depth);

        SampleHistory result;
        sample_history_decode(result, temporal_sample);

        temporal_history.lighting += result.lighting * weight;
        temporal_history.variance += result.variance * weight;
        fast_history += fast_sample * weight;

        weight_sum += weight;
    }

    weight_sum = 1.0f / max(0.0001f, weight_sum);

    temporal_history.lighting *= weight_sum;
    temporal_history.variance *= weight_sum;
    fast_history *= weight_sum;
}

#if PH_RESTIR_ACCUMULATION_FRAMES > 4
float sample_history_min_variance(float samples) {
    const float high_variance = 10.0f;

    if (samples > 4f) return 0.0001f;
    if (samples > 2f) return 0.01f;
    if (frag_is_hand) return high_variance;

    const float padding = 0.13f;
    const vec2 min = vec2(0 + padding) * PH_RENDER_SCALE;
    const vec2 max = vec2(1.0 - padding) * PH_RENDER_SCALE;

    vec2 uv = gl_FragCoord.xy * (vec2(1.0f) / vec2(viewWidth, viewHeight));
    return clamp(uv, min, max) != uv ? high_variance : 0.01f;
}
#else
float sample_history_min_variance(float samples) {
    return 1.0f;
}
#endif

void sample_history_add_sample(inout SampleHistory history, inout vec4 fast_history, vec3 smple) {
#if PH_RESTIR_DENOISER_PASSES <= 0
    smple /= get_exposure();
#endif

    history.lighting.w = min(history.lighting.w, PH_RESTIR_ACCUMULATION_FRAMES);
    float mix_factor = 1f / (++history.lighting.w);

    history.lighting.rgb = mix(history.lighting.rgb, smple, mix_factor);

    // variance
#if PH_RESTIR_DENOISER_PASSES > 0
    vec2 moments = vec2(dot(smple, vec3(0.299, 0.587, 0.114)));
    moments.y = moments.x * moments.x;

    history.variance.xy = mix(history.variance.xy, moments, mix_factor);
    history.variance.w = 1f;

    history.variance.z = max(
            history.variance.y - (history.variance.x * history.variance.x),

            // With few samples, variance estimate is unreliable — use a high floor
            sample_history_min_variance(history.lighting.a)
    ) * mix_factor;
#endif

#if PH_RESTIR_ACCUMULATION_FRAMES >= 12
    const float fast_history_samples = min(floor(PH_RESTIR_ACCUMULATION_FRAMES * 0.25f), 8);
    const float fast_history_cutoff = fast_history_samples * 2.0f;

    const float fast_history_weight = 2.0f;
    const float rcp_fast_history_weight = 1.0f / fast_history_weight;

    fast_history.w = min(fast_history.w, fast_history_samples);
    fast_history.rgb = mix(fast_history.rgb, smple, 1f / (++fast_history.w));

    if (history.lighting.a > fast_history_cutoff) {
        history.lighting.rgb = min(history.lighting.rgb, fast_history.rgb * fast_history_weight);
        history.lighting.rgb = max(history.lighting.rgb, fast_history.rgb * rcp_fast_history_weight);
    }
#endif
}
#endif
