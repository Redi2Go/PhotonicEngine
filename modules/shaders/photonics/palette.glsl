#ifndef PH_PALETTE_INCLUDE
#define PH_PALETTE_INCLUDE

#include "/photonics/utility/color.glsl"
#include "/photonics/utility/normal_encoding.glsl"
#include "/photonics/modifiers/voxel_color_modifier.glsl"

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
    const float rcp_255 = 1.0f / 255.0f;
    vec4 albedo = vec4(ph_unpack_int_color(voxel_data.y)) * rcp_255;

#if !defined PH_VOXEL_COLOR_MODIFIER_DISABLED

#if defined VOXEL_COLOR_MODIFIER_SIMPLE
    voxel_color_modifier(albedo);
#else
    vec3 temp;
    ivec3 temp2;

    voxel_color_modifier(albedo, temp, temp2);
#endif

#endif

    return albedo;
}

vec4 voxel_data_normal(VoxelData voxel_data) {
    const float rcp_255 = 1.0f / 255.0f;
    return vec4(ph_unpack_int_color(voxel_data.z)) * rcp_255;
}

vec4 voxel_data_specular(VoxelData voxel_data) {
    const float rcp_255 = 1.0f / 255.0f;
    return vec4(ph_unpack_int_color(voxel_data.w)) * rcp_255;
}



layout (std430) restrict readonly buffer ph_palette_texture {
    uvec4 ph_palette_buffer[];
};

VoxelData ph_fetch_voxel_data(uint palette_entry, VoxelNormal normal) {
    return ph_palette_buffer[palette_entry + normal];
}


#endif
