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

#endif
