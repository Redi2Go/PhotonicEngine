#ifndef PH_RAND_UTILITY_INCLUDE
#define PH_RAND_UTILITY_INCLUDE

uint ph_new_rand_state(vec2 frag, int frame, int seed) {
    return uint(
        uint(gl_FragCoord.x) * uint(1973) +
        uint(gl_FragCoord.y) * uint(9277) +
        uint(frame + 31 *  seed) *
        uint(26699)
    ) | uint(1);
}

uint ph_rand_next_uint(inout uint rand_state)
{
    uint state = rand_state;
    rand_state = rand_state * 747796405u + 2891336453u;
    uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    return (word >> 22u) ^ word;
}

float ph_rand_next_float(inout uint rand_state) {
    uint x = ph_rand_next_uint(rand_state);
    rand_state = x;

    return float(x) * uintBitsToFloat(0x2f800000u);
}

int ph_rand_next_int(inout uint rand_state, float min, float max) {
    return int(min + (ph_rand_next_float(rand_state) * (max - min)));
}

const float ph_light_jitter_radius = 1.0f / 8.0f;

void ph_rand_sample_position(inout uint rand_state, inout vec3 light_position, vec3 sample_pos) {
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

    light_position = light_position + disk_point.x * sample_tangent + disk_point.y * sample_bitangent;
}

// Thanks null!
vec3 ph_sample_cosine_weighted_hemisphere(inout uint rnd_state) {
    const float pi = 3.14159265359f;

    vec2 u = vec2(
        ph_rand_next_float(rnd_state),
        ph_rand_next_float(rnd_state)
    );

    float r = sqrt(u.x);
    float theta = (2.0 * pi) * u.y;

    return vec3(r * cos(theta), r * sin(theta), sqrt(max(0.0, 1.0 - u.x)));
}
vec3 ph_rand_direction(inout uint state, vec3 normal)
{
    vec3 local_dir = ph_sample_cosine_weighted_hemisphere(state);

    vec3 up = abs(normal.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);

    vec3 tangent = normalize(cross(up, normal));
    vec3 bitangent = cross(normal, tangent);

    return mat3(tangent, bitangent, normal) * local_dir;
}

#endif
