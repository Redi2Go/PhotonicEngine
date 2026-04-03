#include "/photonics/photonics.glsl"
#include "/photonics/common/util.glsl"

#include "/photonics/common/indirect_lighting.glsl"
#include "/photonics/common/handheld_lighting.glsl"

void sample_indirect() {
    vec3 sample_position = world_pos;
    ivec3 write = ph_write(sample_position, block_normal, modelview_projection, world_camera_position);

    uint w = imageAtomicAdd(gi_w, write, uint(1));
    if (w == 0) {
        ivec3 read = ph_read(sample_position, block_normal, previous_modelview_projection, previous_world_camera_position);

        vec4 result = vec4(0.0f);
        result.x += imageLoad(gi_x, read).x / 255.0f;
        result.y += imageLoad(gi_y, read).x / 255.0f;
        result.z += imageLoad(gi_z, read).x / 255.0f;
        result.w = imageLoad(gi_w, read).x;

        result *= 0.975f; // exponential decay

        imageAtomicAdd(gi_x, write, uint(result.x * 255.0f));
        imageAtomicAdd(gi_y, write, uint(result.y * 255.0f));
        imageAtomicAdd(gi_z, write, uint(result.z * 255.0f));
        imageAtomicAdd(gi_w, write, uint(result.w));
    } else if (w < 2048) {
        vec3 result = ph_sample_indirect_impl();

        imageAtomicAdd(gi_x, write, uint(result.x * 255.0f));
        imageAtomicAdd(gi_y, write, uint(result.y * 255.0f));
        imageAtomicAdd(gi_z, write, uint(result.z * 255.0f));
    } else {
        imageAtomicAdd(gi_w, write, uint(-1));
    }
}