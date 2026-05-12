#ifndef PH_LIGHT_LIST_INCLUDE
#define PH_LIGHT_LIST_INCLUDE

#include "/photonics/uniforms.glsl"
#include "/photonics/internal/light.glsl"

layout (std140) restrict readonly buffer ph_light_list {
// vec 1: position (xyz) + block_id (w)
// vec 2: color (xyz) + intensity (w)
// vec 3: attenutation (xy) + falloff (z) + block_radius (w)
    vec4 ph_lights_array[];
};

const int ph_light_size = 3; // 3 vec4s per light

Light load_light(int index) {
    int real_index = index;
    index*= ph_light_size;

    return new_light_from_vec4(
        ph_lights_array[index + 0],
        ph_lights_array[index + 1],
        ph_lights_array[index + 2],
        real_index
    );
}

#endif