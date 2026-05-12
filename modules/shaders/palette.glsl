#ifndef PH_PALETTE_INCLUDE
#define PH_PALETTE_INCLUDE

#include "/photonics/utility/color.glsl"
#include "/photonics/utility/normal_encoding.glsl"

// VoxelData common

#define VoxelData uvec4

void voxel_data_apply_tint(inout VoxelData voxel_data, uvec4 tint) {
     voxel_data.y = ph_pack_int_color(
        ph_apply_int_tint(
            ph_unpack_int_color(voxel_data.y),
            tint
        )
    );
}

int voxel_data_block_id(VoxelData voxel_data) {
    return int(voxel_data.x);
}

vec4 voxel_data_albedo(VoxelData voxel_data) {
    return vec4(ph_unpack_int_color(voxel_data.y)) / 255f;
    //return unpackUnorm4x8(voxel_data.y);
}

vec4 voxel_data_normal(VoxelData voxel_data) {
    return unpackUnorm4x8(voxel_data.z);
}

vec4 voxel_data_specular(VoxelData voxel_data) {
    return unpackUnorm4x8(voxel_data.w);
}



layout (std430) restrict readonly buffer ph_palette_texture {
    uvec4 ph_palette_buffer[];
};

VoxelData ph_fetch_voxel_data(uint palette_entry, VoxelNormal normal) {
    return ph_palette_buffer[palette_entry + normal];
}


#endif