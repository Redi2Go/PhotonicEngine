#ifndef PH_TRACING_COMMON_INCLUDE
#define PH_TRACING_COMMON_INCLUDE

#include "/photonics/uniforms.glsl"
#include "/photonics/palette.glsl"
#include "/photonics/internal/light.glsl"
#include "/photonics/utility/color.glsl"
#include "/photonics/utility/normal_encoding.glsl"

#define PH_WORLD_DEPTH 4
#define ph_is_data(entry) (entry & PH_SIGN_BIT) != 0u

#define PH_BLOCK_DEPTH 0
#define PH_CHUNK_DEPTH 1

const int PH_LAST_INDEX = PH_WORLD_DEPTH - 1;
const uint PH_SIGN_BIT = 1 << 31;

const ivec3 PH_WORLD_BOUNDS = ivec3(16 << (PH_LAST_INDEX << 2));

struct RayResult {
    uint _x;
    uint _y;
    uint _z;

    uint _data1;
    uint _data2;
};

RayResult ph_ray_miss = RayResult(
    0,
    0,
    0,
    0,
    0
);

const uint ph_ray_result_position_mask = 0x7fffffffu;

const uvec3 ph_ray_result_normal_shift = uvec3(31, 30, 29);
const uvec3 ph_ray_result_normal_mask = uvec3(1, 2, 4);

const uint ph_ray_result_palette_mask = 0xffffffu;

RayResult new_ray_result(
    vec3 position,
    VoxelNormal normal,
    uint palette_header_ptr,
    uint light_ptr,
    bool transparent
) {
    uvec3 pos = floatBitsToUint(position) | ((uvec3(normal) & ph_ray_result_normal_mask) << ph_ray_result_normal_shift);

    return RayResult(
        pos.x,
        pos.y,
        pos.z,
        palette_header_ptr | (transparent ? PH_SIGN_BIT : 0),
        light_ptr
    );
}

RayResult missed_ray_result() {
    return ph_ray_miss;
}

bool ray_result_is_hit(RayResult hit) {
    return hit._data1 != 0;
}

vec3 ray_result_position(RayResult hit) {
    return uintBitsToFloat(uvec3(hit._x, hit._y, hit._z) & ph_ray_result_position_mask);
}

VoxelNormal _ray_result_voxel_normal(RayResult hit) {
    uvec3 normal_bits = (uvec3(hit._x, hit._y, hit._z) >> ph_ray_result_normal_shift) & ph_ray_result_normal_mask;
    return normal_bits.x | normal_bits.y | normal_bits.z;
}

vec3 ray_result_normal(RayResult hit) {
    return ph_decode_voxel_normal(_ray_result_voxel_normal(hit));
}

bool ray_result_is_transparent(RayResult hit) {
    return (hit._data1 & PH_SIGN_BIT) != 0;
}


layout (std430) restrict readonly buffer ph_world_voxel_buffer {
    uint ph_world_buffer[];
};

VoxelData ray_result_voxel_data(RayResult hit) {
    uint palette_ptr = hit._data1 & ~PH_SIGN_BIT;

    uint tint = ph_world_buffer[palette_ptr];
    uint palette_entry = ph_world_buffer[palette_ptr + 1];

    VoxelData voxel_data = ph_fetch_voxel_data(palette_entry, _ray_result_voxel_normal(hit));
    voxel_data_apply_tint(voxel_data, ph_unpack_int_color(tint));

    return voxel_data;
}

Light ray_result_light_data(RayResult hit) {
    if (hit._data2 == 0) return new_invalid_light();

    uint light_ptr = hit._data2;

    uint light_type = ph_world_buffer[light_ptr];
    uint block_id = ph_world_buffer[light_ptr + 1];

    uvec4 color_full = uvec4(
        ph_world_buffer[light_ptr + 2],
        ph_world_buffer[light_ptr + 3],
        ph_world_buffer[light_ptr + 4],
        ph_world_buffer[light_ptr + 5]
    );

    uvec4 attenuation_full = uvec4(
        ph_world_buffer[light_ptr + 6],
        ph_world_buffer[light_ptr + 7],
        ph_world_buffer[light_ptr + 8],
        ph_world_buffer[light_ptr + 9]
    );

    return new_light_from_vec4(
        vec4(ray_result_position(hit), uintBitsToFloat(block_id)),
        uintBitsToFloat(color_full),
        uintBitsToFloat(attenuation_full),
        0, // index
        int(light_type)
    );
}

float ph_signed_nudge(float value) {
    if (value <= 0f)
        return min(value, -0.0001f);
    else if (value >= 0)
        return max(value, 0.0001);
    else
        return value;
}

vec3 ph_signed_nudge(vec3 value) {
    return vec3(
        ph_signed_nudge(value.x),
        ph_signed_nudge(value.y),
        ph_signed_nudge(value.z)
    );
}

bool ph_is_inside(vec3 position) {
    vec3 s = step(world_min_voxel, position) - step(world_max_voxel, position);
    return bool(s.x * s.y * s.z);
}

float ph_intersects_world(vec3 direction_inv, vec3 origin) {
    if (ph_is_inside(origin)) {
        return 0.0f;
    }

    vec3 tbot = direction_inv * (world_min_voxel - origin);
    vec3 ttop = direction_inv * (world_max_voxel - origin);
    vec3 tmin = min(ttop, tbot);
    vec3 tmax = max(ttop, tbot);
    vec2 t = max(tmin.xx, tmin.yz);
    float t0 = max(t.x, t.y);
    t = min(tmax.xx, tmax.yz);
    float t1 = min(t.x, t.y);

    return t1 > max(t0, 0.0f) ? t0 : -1.0f;
}

int ph_to_fake_air_entry(ivec3 pos) {
    int value = (pos.x << 0) |(pos.y << 5) | (pos.z << 10);

    return value | (value << 15);
}

const int[] morton = int[](0, 1, 8, 9, 64, 65, 72, 73, 512, 513, 520, 521, 576, 577, 584, 585, 4096,
4097, 4104, 4105, 4160, 4161, 4168, 4169, 4608, 4609, 4616, 4617, 4672, 4673, 4680, 4681);

int ph_get_voxel_index(ivec3 position) {
    return morton[position.x] | (morton[position.y] << 1) | (morton[position.z] << 2);
}

#endif