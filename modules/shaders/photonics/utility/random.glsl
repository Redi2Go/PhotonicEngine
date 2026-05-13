#ifndef PH_RAND_UTILITY_INCLUDE
#define PH_RAND_UTILITY_INCLUDE

uint new_rand_state(vec2 frag, int frame, int seed) {
    return uint(
        uint(gl_FragCoord.x) * uint(1973) +
        uint(gl_FragCoord.y) * uint(9277) +
        uint(frame + 31 *  seed) *
        uint(26699)
    ) | uint(1);
}

uint rand_next_uint(inout uint rand_state)
{
    uint state = rand_state;
    rand_state = rand_state * 747796405u + 2891336453u;
    uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    return (word >> 22u) ^ word;
}

float rand_next_float(inout uint rand_state) {
    uint x = rand_next_uint(rand_state);
    rand_state = x;

    return float(x) * uintBitsToFloat(0x2f800000u);
}

int rand_next_int(inout uint rand_state, float min, float max) {
    return int(min + (rand_next_float(rand_state) * (max - min)));
}

const float ph_light_jitter_radius = 1.0f / 8.0f;

vec3 rand_sample_position(inout uint rand_state, inout vec3 light_position, vec3 sample_pos) {
    light_position = floor(light_position) + 0.5f;

    // Fetch a blue noise value for this frame.
    vec2 rnd_sample      = vec2(rand_next_float(rand_state), rand_next_float(rand_state));

    vec3 sample_dir = light_position - sample_pos;

    vec3 sample_tangent   = normalize(cross(sample_dir, normalize(vec3(0.0f, 1.0f, 1.0f))));
    vec3 sample_bitangent = normalize(cross(sample_tangent, sample_dir));

    // calculate disk point
    float point_radius = ph_light_jitter_radius * sqrt(rnd_sample.x);

    float point_angle  = rnd_sample.y * 2.0f * 3.14159265f;
    vec2  disk_point   = vec2(point_radius * cos(point_angle), point_radius * sin(point_angle));

    light_position = light_position + disk_point.x * sample_tangent + disk_point.y * sample_bitangent;

    return light_position;
}

vec3 rand_direction(inout uint state, vec3 normal)
{
    const float c_pi = 3.14159265359f;
    const float c_twopi = 2.0f * c_pi;

    float z = rand_next_float(state) * 2.0f - 1.0f;
    float a = rand_next_float(state) * c_twopi;
    float r = sqrt(1.0f - z * z);
    float x = r * cos(a);
    float y = r * sin(a);
    return normalize(vec3(x, y, z) + normal);
}

#endif