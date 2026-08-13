#ifndef PH_RAND_UTILITY_INCLUDE
#define PH_RAND_UTILITY_INCLUDE

uint ph_new_rand_state(vec2 frag, int frame, int seed) {
    return uint(
        uint(frag.x) * uint(1973) +
        uint(frag.y) * uint(9277) +
        uint(seed)   * uint(26699) +
        uint(frame)  * uint(69193)
    ) | uint(1);
}

uint ph_rand_next_uint(inout uint rand_state)
{
    uint state = rand_state;
    rand_state = rand_state * 747796405u + 2891336453u;
    uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;

    rand_state = word;
    return (word >> 22u) ^ word;
}

float ph_rand_next_float(inout uint rand_state) {
    return float(ph_rand_next_uint(rand_state)) * uintBitsToFloat(0x2f800000u);
}

int ph_rand_next_int(inout uint rand_state, float min, float max) {
    return int(min + (ph_rand_next_float(rand_state) * (max - min)));
}

const float ph_light_jitter_radius = 1.0f / 16.0f;

vec3 ph_rand_sample_position(inout uint rand_state, vec3 light_position, vec3 sample_pos) {
    light_position = floor(light_position) + 0.5f;

    // Fetch a blue noise value for this frame.
    vec2 rnd_sample      = vec2(ph_rand_next_float(rand_state), ph_rand_next_float(rand_state));

    vec3 sample_dir = light_position - sample_pos;

    vec3 sample_tangent   = normalize(cross(sample_dir, normalize(vec3(0.0f, 1.0f, 1.0f))));
    vec3 sample_bitangent = normalize(cross(sample_tangent, sample_dir));

    // calculate disk point
    float point_radius = ph_light_jitter_radius * sqrt(rnd_sample.x);

    float point_angle  = rnd_sample.y * 2.0f * 3.14159265f;
    vec2  disk_point   = vec2(point_radius * cos(point_angle), point_radius * sin(point_angle));

    return disk_point.x * sample_tangent + disk_point.y * sample_bitangent;
}

// Random direction code from zephyr starlight
// This produces the most unbiased result for restir
// No idea why either
float ph_rand_dist(inout uint state) {
    const float c_pi = 3.14159265359f;
    const float c_twopi = 2.0f * c_pi;

    return sqrt(-log2(ph_rand_next_float(state))) * cos(c_twopi * ph_rand_next_float(state));
}

vec3 ph_rand_direction(inout uint state, vec3 normal) {
    vec3 dir = normalize(vec3(ph_rand_dist(state), ph_rand_dist(state), ph_rand_dist(state)));
    return normalize(dir + normal);
}

#endif
