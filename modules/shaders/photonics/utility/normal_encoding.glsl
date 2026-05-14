#ifndef PH_NORMAL_ENCODING_UTIL_INCLUDE
#define PH_NORMAL_ENCODING_UTIL_INCLUDE

#define VoxelNormal uint
const vec3[6] ph_index_to_normal_mapping = vec3[6](
    vec3(-1, 0, 0),
    vec3(1, 0, 0),
    vec3(0, -1, 0),
    vec3(0, 1, 0),
    vec3(0, 0, -1),
    vec3(0, 0, 1)
);

// Encodes a normal into an int 0-5, provided by Merlin
VoxelNormal ph_encode_voxel_normal(vec3 normal) {
    return uint(clamp(
        int(
                abs(normal.x)*(normal.x*0.5+0.5)
                + abs(normal.y)*(normal.y*0.5+2.5)
                + abs(normal.z)*(normal.z*0.5+4.5)
                + 0.5
        ),
        0,
        5
    ));
}

vec3 ph_decode_voxel_normal(VoxelNormal normal) {
    return ph_index_to_normal_mapping[normal];
}

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

#endif
