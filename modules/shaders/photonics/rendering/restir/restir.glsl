#ifndef PH_SHARED_INCLUDE
#define PH_SHARED_INCLUDE

#include "/photonics/utility/color.glsl"

#include "/photonics/rendering/restir/direct/reservoir.glsl"
#include "/photonics/rendering/restir/indirect/reservoir.glsl"

#include "/photonics/utility/projection.glsl"
#include "/photonics/utility/normal_encoding.glsl"

#define RESTIR_LIGHTING_OUT 0
#define RESTIR_LIGHTING_VARIANCE_OUT 1
#define RESTIR_LIGHTING_SAMPLES_OUT 2

//ph_required: uniform sampler2D restir_lighting;
//ph_required: uniform sampler2D restir_lighting_variance;
//ph_required: uniform sampler2D restir_lighting_samples;

//ph_required: uniform sampler2D prev_restir_lighting;
//ph_required: uniform sampler2D prev_restir_lighting_variance;

struct SampleHistory {
    vec4 lighting;
    vec4 variance;
};

const float INVALID_SAMPLE_COMPONENT = -999.0f;
const SampleHistory INVALID_HISTORY = SampleHistory(vec4(INVALID_SAMPLE_COMPONENT), vec4(INVALID_SAMPLE_COMPONENT));

bool sample_history_is_valid(SampleHistory history) {
    return history.lighting.x != INVALID_SAMPLE_COMPONENT;
}

void sample_history_load(out SampleHistory smple) {
    smple.lighting = texelFetch(restir_lighting, frag_tex_coord, 0),
    smple.variance = vec4(0f);
}

SampleHistory sample_history_mix(SampleHistory s1, SampleHistory s2, float a) {
    if (!sample_history_is_valid(s1)) {
        a = 1f;
    } else if (!sample_history_is_valid(s2)) {
        a = 0f;
    } else if (!sample_history_is_valid(s1) && !sample_history_is_valid(s2)) {
        return INVALID_HISTORY;
    }

    return SampleHistory(
        mix(s1.lighting, s2.lighting, a),
        mix(s1.variance, s2.variance, a)
    );
}

SampleHistory sample_history_reproject_single(ivec2 texel, vec3 previous_player_pos, float distance_factor) {
    FragData prev_frag;
    frag_data_load_previous(prev_frag, texel);

    if (!frag_is_bad_angle) {
        vec3 projected_player_pos = frag_data_player_pos(prev_frag);
        vec3 d = projected_player_pos - previous_player_pos;

        if (dot(d, d) > distance_factor) return INVALID_HISTORY;
    }

    vec3 n = frag_data_geo_normal(prev_frag);
    if (dot(n, frag_geo_normal) < 0.99f) return INVALID_HISTORY;

    vec4 lighting = texelFetch(prev_restir_lighting, ivec2(texel), 0);
    if (any(isnan(lighting))) return INVALID_HISTORY;

    vec4 variance = texelFetch(prev_restir_lighting_variance, ivec2(texel), 0);
    if (any(isnan(variance))) return INVALID_HISTORY;

    return SampleHistory(lighting, variance);
}

SampleHistory sample_history_reproject_mixed(vec2 center, vec3 previous_player_pos, float distance_factor) {
    ivec2 icenter = ivec2(center);

    SampleHistory c_00 = sample_history_reproject_single(icenter + ivec2(0, 0), previous_player_pos, distance_factor);
    SampleHistory c_10 = sample_history_reproject_single(icenter + ivec2(1, 0), previous_player_pos, distance_factor);
    SampleHistory c_01 = sample_history_reproject_single(icenter + ivec2(0, 1), previous_player_pos, distance_factor);
    SampleHistory c_11 = sample_history_reproject_single(icenter + ivec2(1, 1), previous_player_pos, distance_factor);

    SampleHistory result = sample_history_mix(
        sample_history_mix(c_00, c_10, fract(center.x)),
        sample_history_mix(c_01, c_11, fract(center.x)),
        fract(center.y)
    );

    if (!sample_history_is_valid(result))
        return SampleHistory(vec4(0.0f), vec4(0.0f));

    return result;
}

void sample_history_reproject(out SampleHistory smple) {
    vec3 previous_player_pos = frag_rt_pos - rt_camera_position;

    const float block_divsor = 64.0f * PH_RENDER_SCALE;
    float distance_factor = max(dot(previous_player_pos, previous_player_pos) / block_divsor, 0.1f);

    vec2 center = (ph_reproject_player_pos(
        frag_player_pos,
        frag_is_hand,
        get_taa_jitter(),
        previous_player_pos
    ).xy * PH_VIEW_SIZE) - 0.5f;

    smple = sample_history_reproject_mixed(center, previous_player_pos, distance_factor);
}

void sample_history_combine_lighting(inout SampleHistory history, in SampleHistory smple) {
#if PH_RESTIR_DENOISER_PASSES != 0
    history.lighting.w = min(history.lighting.w, PH_RESTIR_ACCUMULATION_FRAMES);
    history.lighting.rgb = mix(history.lighting.rgb, smple.lighting.rgb, 1f / (++history.lighting.w));
#else
    if (history.lighting.a >= PH_RESTIR_ACCUMULATION_FRAMES - 1f)
        history.lighting *= ((PH_RESTIR_ACCUMULATION_FRAMES - 1f) / history.lighting.a);

    history.lighting.rgb+= smple.lighting.rgb;
    history.lighting.a++;
#endif
}

void sample_history_combine_moment(inout SampleHistory history, in SampleHistory smple) {
    float moment_alpha = 1f / history.lighting.a;
    vec2 moments = vec2(0f);

    moments.x = dot(smple.lighting.rgb, vec3(0.299, 0.587, 0.114));
    moments.y = moments.x * moments.x;

    history.variance.xy = mix(history.variance.xy, moments, moment_alpha);
    history.variance.w = 1f;
}

void sample_history_compute_variance(inout SampleHistory history, in SampleHistory smple) {
#if PH_RESTIR_ACCUMULATION_FRAMES < 4
    #define PH_MIN_VARIANCE 1f
#else
    #define PH_MIN_VARIANCE 0.01f
#endif

    float samples = history.lighting.a;
    float sample_variance = max(
        history.variance.y - (history.variance.x * history.variance.x),

        // With few samples, variance estimate is unreliable — use a high floor
        PH_MIN_VARIANCE
    );

    history.variance.z = sample_variance / samples;
}

#endif
