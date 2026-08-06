#define SVGF_HISTORY_OUT 0

uniform usampler2D diffuse_history;
uniform usampler2D prev_diffuse_history;

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
    value.x = packHalf2x16(history.lighting.xy);
    value.y = packHalf2x16(history.lighting.zw);

    value.z = packHalf2x16(history.variance.xy);
    value.w = packHalf2x16(history.variance.zw);
}

void sample_history_decode(out SampleHistory history, uvec4 value) {
    history.lighting.xy = unpackHalf2x16(value.x);
    history.lighting.zw = unpackHalf2x16(value.y);

    history.variance.xy = unpackHalf2x16(value.z);
    history.variance.zw = unpackHalf2x16(value.w);
}

void sample_history_reproject(out SampleHistory history) {
    const ivec2[4] offsets = ivec2[](ivec2(0, 0), ivec2(1, 0), ivec2(0, 1), ivec2(1, 1));

    vec3 dist = frag_rt_pos - rt_camera_position;
    const float block_divsor = 64.0f * PH_RENDER_SCALE;
    float distance_factor = max(dot(dist, dist) / block_divsor, 0.1f);

    vec2 center = ph_reproject_player_pos(frag_player_pos, frag_is_hand, get_taa_jitter()).xy;
         center*= PH_VIEW_SIZE;
         center-= 0.5f;

    ivec2 centerTexel = ivec2(center);
    vec2 mix_factor = fract(center);

    float weight_sum = 0.0f;
    for (int i = 0; i < offsets.length(); i++) {
        ivec2 texel = centerTexel + offsets[i];
        if (any(lessThan(texel, ivec2(0)))) continue;
        if (any(greaterThan(texel, textureSize(diffuse_history)))) continue;

        vec2 mixWeight = vec2(ivec2(1) - offsets[i]) - mix_factor;
        float weight = mixWeight.x * mixWeight.y;

        FragData prev_frag;
        frag_data_load_previous(prev_frag, texel);

        if (!frag_is_bad_angle) {
            vec3 d = frag_data_player_pos(prev_frag) - frag_player_pos;
            if (dot(d, d) > distance_factor) weight = 0.0f;
        }

        vec3 n = frag_data_geo_normal(prev_frag);
        if (dot(n, frag_geo_normal) < 0.99f) weight = 0.0f;

        SampeHistory smple;
        sample_history_decode(smple, texelFetch(diffuse_history, texel, 0));

        if (sample_history_is_valid(smple)) {
            history.lighting += smple.lighting * weight;
            history.variance += smple.variance * weight;

            weight_sum+= weight;
        }
    }

    weight_sum = 1.0 / max(0.0001f, weight_sum);

    history.lighting *= weight_sum;
    history.variance *= weight_sum;
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

void sample_history_add_sample(inout SampleHistory history, vec3 smple) {
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
}
