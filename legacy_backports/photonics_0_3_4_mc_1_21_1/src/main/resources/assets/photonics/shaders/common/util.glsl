#ifndef PH_UTIL_INCLUDE
#define PH_UTIL_INCLUDE

const float ph_light_jitter_radius = 1.0f / 16.0f;

float rand_next_float() {
    return ph_RandomFloat01(rng_state);
}

int rand_next_int(float min, float max) {
    return int(min + (rand_next_float() * (max - min)));
}

const float F = 2.0f;

// Thanks null!
float ph_saturate(const in float x) { return clamp(x, 0.0, 1.0); }

float ph_sum_of(vec2 vec) { return vec.x + vec.y; }
float ph_sum_of(vec3 vec) { return vec.x + vec.y + vec.z; }

vec2 ph_oct_wrap(const in vec2 v) {
    return (1.0 - abs(v.yx)) * (step(0.0, v.xy) * 2.0 - 1.0);
}

vec2 ph_encode_normal(vec3 n) {
    n /= ph_sum_of(abs(n));
    n.xy = n.z >= 0.0 ? n.xy : ph_oct_wrap(n.xy);
    n.xy = n.xy * 0.5 + 0.5;
    return n.xy;
}

vec3 ph_decode_normal(vec2 f) {
    f = f * 2.0 - 1.0;

    // https://twitter.com/Stubbesaurus/status/937994790553227264
    vec3 n = vec3(f.xy, 1.0 - ph_sum_of(abs(f.xy)));
    float t = ph_saturate(-n.z);
    n.xy += mix(vec2(t), vec2(-t), step(0.0, n.xy));
    return normalize(n);
}
// end of thanks null

vec3 ph_get_lighting_normal(vec4 packed_normal) {
    return ph_decode_normal(ph_frag_is_hand ? packed_normal.xy : packed_normal.zw);
}

// Pulse function
float ph_g(float x) {
    return sin(F * 3.141592f * clamp(x, 0.0f, 1.0f / F));
}

// Noise
float ph_n(float x) {
    return 0.5f * sin(1000.0f * x) + 0.5f;
}

// Periodic function with random offset
float ph_h(float x) {
    return ph_g(fract(x) - (1.0f - 1.0f / F) * ph_n(floor(x)));
}

float ph_luminance(vec3 rgb) {
    return dot(rgb, vec3(0.2126f, 0.7152f, 0.0722f));
}

void jitter_sample_position(inout vec3 position) {
    vec3 light_position = floor(position) + 0.5f;

    // Fetch a blue noise value for this frame.
    vec2 rnd_sample      = vec2(rand_next_float(), rand_next_float());

    vec3 light_dir       = light_position - ray.origin;

    vec3 light_tangent   = normalize(cross(light_dir, normalize(vec3(0.0f, 1.0f, 1.0f))));
    vec3 light_bitangent = normalize(cross(light_tangent, light_dir));

    // calculate disk point
    float point_radius = ph_light_jitter_radius * sqrt(rnd_sample.x);

    float point_angle  = rnd_sample.y * 2.0f * 3.14159265f;
    vec2  disk_point   = vec2(point_radius * cos(point_angle), point_radius * sin(point_angle));

    position = light_position + disk_point.x * light_tangent + disk_point.y * light_bitangent;
}

bool is_bad_angle(vec3 world_pos, vec3 normal) {
    float dist = distance(floor(world_pos), floor(world_camera_position));

    float resolution = ceil((dist / 16)) / (4 * PH_RENDER_SCALE);
    ray_normal_scale = 0.01f + (0.04f * resolution);

    return dot(normal, normalize(world_pos - world_camera_position)) > -0.2f && dist > 16.0f;
}
#endif