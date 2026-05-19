#ifndef PH_RESTIR_INCLUDE
#define PH_RESTIR_INCLUDE

#include "/photonics/tracing.glsl"
#include "/photonics/light_list.glsl"
#include "/photonics/utility/random.glsl"
#include "/photonics/utility/color.glsl"
#include "/photonics/utility/projection.glsl"

uniform sampler2D restir_reservoirs;
uniform sampler2D restir_lighting;
uniform sampler2D restir_lighting_variance;
uniform sampler2D restir_lighting_samples;

uniform sampler2D prev_restir_reservoirs;
uniform sampler2D prev_restir_lighting;
uniform sampler2D prev_restir_lighting_variance;


uniform sampler2D denoise_color;
uniform sampler2D denoise_variance;

uniform sampler2D prev_denoise_color;
uniform sampler2D prev_denoise_variance;

uniform sampler2D restir_position_history;
uniform sampler2D restir_normal_history;

uniform sampler2D prev_restir_position_history;
uniform sampler2D prev_restir_normal_history;

float light_importance = 1f / light_list_size;
const float MAX_RESERVOIR_SAMPLES = 20f * PH_RESTIR_INITIAL_SAMPLES;

struct LightSample {
    int index; // Index of the light
    vec3 light_pos; // The position of the light
    vec3 sample_pos;
    vec3 color; // The sampled color
    vec3 dir; // Normalized direction from the fragment to the light

    float weight;
};

LightSample NULL_SAMPLE = LightSample(-1, vec3(0f), vec3(0f), vec3(0f), vec3(0f), 0f);

void light_sample_compute_weight(inout LightSample smple) {
    smple.weight = ph_luminance(smple.color);
}

LightSample light_sample_new(Light light, vec3 sample_pos) {
    LightSample result = LightSample(
        light.index,
        light.position,
        sample_pos,
        vec3(0f),
        light.position - sample_pos,
        0f
    );

    result.color = ph_compute_attenuation(
        light,
        result.dir,
        sample_pos,
        light.position,
        frag_geo_normal,
        frag_is_hand ? frag_geo_normal : frag_tex_normal
    );

    result.dir = normalize(result.dir);
    light_sample_compute_weight(result);

    // light_sample_compute_weight uses luminance
    // originally was ph_luminance(result.color) < 0.0001f
    if (result.weight < 0.0001f) {
        result.color = vec3(0f);
        result.weight = 0f;

        return result;
    }

    return result;
}

void light_sample_trace_hit(inout LightSample smple, out vec3 tint_color, out float light_transmittance, bool jitter) {
    if (jitter) {
        rand_sample_position(frag_rnd_state, smple.light_pos, smple.sample_pos);
        smple.dir = normalize(smple.light_pos - smple.sample_pos);
    }

    if (!trace_light_vis(smple.sample_pos, smple.dir, smple.light_pos, 20, tint_color, light_transmittance)) {
        smple.color = vec3(0f);
        smple.weight = 0f;

        return;
    }

    light_sample_compute_weight(smple); // Update weight for reuse.
}

float light_sample_encode(LightSample smple) {
    return float(smple.index);
}

LightSample light_sample_decode(float value, vec3 sample_pos, bool remap) {
    int index = remap ? light_list_map_index(int(value)) : int(value);
    if (index < 0 || index > light_list_size) return NULL_SAMPLE;

    return light_sample_new(light_list_get(index), sample_pos);
}

struct Reservoir {
    LightSample light;
    float weight;
    float weight_sum;
    float samples;
};

Reservoir reservoir_new() {
    return Reservoir(NULL_SAMPLE, 0f, 0f, 0f);
}

Reservoir NULL_RESERVOIR = reservoir_new();

bool reservoir_update(
    inout Reservoir reservoir,
    LightSample smple, // sample is a keyword
    float weight,
    float samples
) {
    reservoir.weight_sum+= weight;
    reservoir.samples+= samples;

    if (rand_next_float(frag_rnd_state) < (weight / reservoir.weight_sum)) {
        reservoir.light = smple;

        return true;
    }

    return false;
}

void reservoir_init(inout Reservoir reservoir, vec3 rt_pos) {
    for (int i = 0; i < 32; i++) {
        int rand_index = rand_next_int(frag_rnd_state, 0, light_list_size);
        LightSample smple = light_sample_new(light_list_get(rand_index), rt_pos);

        reservoir_update(
            reservoir,
            smple,
            smple.weight / light_importance,
            1
        );
    }
}

void reservoir_compute_weight(inout Reservoir reservoir) {
    reservoir.weight = reservoir.light.weight > 0 ?
        (1 / reservoir.light.weight) * (reservoir.weight_sum / reservoir.samples) : 0;
}

bool reservoir_is_valid(Reservoir resevoir) {
    return resevoir.light.index != -1;
}

void reservoir_clamp_samples(inout Reservoir reservoir, float max_samples) {
    if (reservoir.samples < max_samples) return;

    float factor = max_samples / reservoir.samples;

    reservoir.samples = max_samples;
    reservoir.weight_sum *= factor;
}

vec4 reservoir_encode(Reservoir reservoir) {
    return vec4(
        light_sample_encode(reservoir.light),
        reservoir.weight,
        reservoir.weight_sum,
        reservoir.samples
    );
}

void reservoir_decode(inout Reservoir reservoir, vec4 color, vec3 sample_pos, bool remap) {
    reservoir.light = light_sample_decode(color.x, sample_pos, remap);
    reservoir.weight = color.y;
    reservoir.weight_sum = color.z;
    reservoir.samples = color.w;
}

bool reservoir_reuse(inout Reservoir reservoir, ivec2 texel) {
    if (!frag_is_bad_angle) {
        vec3 sample_player_pos = texelFetch(restir_position_history, texel, 0).xyz;
        vec3 d = sample_player_pos - frag_player_pos;
        if (dot(d, d) >= 0.3f) return false;
    }

    vec3 n = ph_decode_normal(texelFetch(restir_normal_history, texel, 0).xy);

    if (dot(n, frag_geo_normal) < 0.99f) return false;

    reservoir_decode(
        reservoir,
        texelFetch(
            restir_reservoirs,
            texel,
            0
        ),
        frag_rt_pos,
        false
    );

    return !isnan(reservoir.weight) && !isnan(reservoir.weight_sum);
}

bool reservoir_reproject(inout Reservoir reservoir) {
    vec3 previous_player_pos;
    vec2 uv = ph_reproject_player_pos(frag_player_pos, frag_is_hand, get_taa_jitter(), previous_player_pos).xy;

    if (clamp(uv, 0, 1) != uv) return false;
    ivec2 prev_texel = ivec2(uv * PH_VIEW_SIZE);

    if (!frag_is_bad_angle) {
        vec3 projected_player_pos = texelFetch(prev_restir_position_history, prev_texel, 0).xyz;
        vec3 d = projected_player_pos - previous_player_pos;
        if (dot(d, d) >= 0.3f) return false;
    }

    vec3 n = ph_decode_normal(texelFetch(prev_restir_normal_history, prev_texel, 0).xy);
    if (dot(n, frag_geo_normal) < 0.99f) return false;

    reservoir_decode(
        reservoir,
        texelFetch(
            prev_restir_reservoirs,
            prev_texel,
            0
        ),
        frag_rt_pos,
        true
    );

    return true;
}

struct SampleHistory {
    vec4 lighting;
    vec4 variance;
};

const SampleHistory NULL_HISTORY = SampleHistory(vec4(-999), vec4(-999));

void sample_history_load(out SampleHistory smple) {
    smple.lighting = texelFetch(restir_lighting, frag_tex_coord, 0),
    smple.variance = vec4(0f);
}

SampleHistory sample_history_mix(SampleHistory s1, SampleHistory s2, float a) {
    if (s1 == NULL_HISTORY) {
        a = 1f;
    } else if (s2 == NULL_HISTORY) {
        a = 0f;
    } else if (s1 == NULL_HISTORY && s2 == NULL_HISTORY) {
        return NULL_HISTORY;
    }

    return SampleHistory(
        mix(s1.lighting, s2.lighting, a),
        mix(s1.variance, s2.variance, a)
    );
}

SampleHistory sample_history_reproject_single(ivec2 texel, vec3 previous_player_pos) {
    if (!frag_is_bad_angle) {
        vec3 projected_player_pos = texelFetch(prev_restir_position_history, texel, 0).xyz;
        vec3 d = projected_player_pos - previous_player_pos;
        if (dot(d, d) > 0.1f) return NULL_HISTORY;
    }

    vec3 n = ph_decode_normal(texelFetch(prev_restir_normal_history, texel, 0).xy);
    if (dot(n, frag_geo_normal) < 0.99f) return NULL_HISTORY;

    vec4 lighting = texelFetch(prev_restir_lighting, ivec2(texel), 0);
    if (any(isnan(lighting))) return NULL_HISTORY;

    vec4 variance = texelFetch(prev_restir_lighting_variance, ivec2(texel), 0);
    if (any(isnan(variance))) return NULL_HISTORY;

    return SampleHistory(lighting, variance);
}

SampleHistory sample_history_reproject_mixed(vec2 center, vec3 previous_player_pos) {
    ivec2 icenter = ivec2(center);

    SampleHistory c_00 = sample_history_reproject_single(icenter + ivec2(0, 0), previous_player_pos);
    SampleHistory c_10 = sample_history_reproject_single(icenter + ivec2(1, 0), previous_player_pos);
    SampleHistory c_01 = sample_history_reproject_single(icenter + ivec2(0, 1), previous_player_pos);
    SampleHistory c_11 = sample_history_reproject_single(icenter + ivec2(1, 1), previous_player_pos);

    SampleHistory result = sample_history_mix(
        sample_history_mix(c_00, c_10, fract(center.x)),
        sample_history_mix(c_01, c_11, fract(center.x)),
        fract(center.y)
    );

    if (result == NULL_HISTORY)
        return SampleHistory(vec4(0.0f), vec4(0.0f));

    return result;
}

void sample_history_reproject(out SampleHistory smple) {
    vec3 previous_player_pos;
    vec2 center = (ph_reproject_player_pos(
        frag_player_pos,
        frag_is_hand,
        get_taa_jitter(),
        previous_player_pos
    ).xy * PH_VIEW_SIZE) - 0.5f;

    smple = sample_history_reproject_mixed(center, previous_player_pos);
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
    float samples = history.lighting.a;
    float sample_variance = max(
        history.variance.y - (history.variance.x * history.variance.x),

        // With few samples, variance estimate is unreliable — use a high floor
        #if PH_RESTIR_ACCUMULATION_FRAMES < 4
        1f
        #else
        (samples < 4f) ? 10f : 0
        #endif
    );

    history.variance.z = sample_variance / samples;
}
#endif