#ifndef PH_RAYTRACING
#define PH_RAYTRACING

//#forward

const int[] morton = int[](0, 1, 8, 9, 64, 65, 72, 73, 512, 513, 520, 521, 576, 577, 584, 585, 4096,
                           4097, 4104, 4105, 4160, 4161, 4168, 4169, 4608, 4609, 4616, 4617, 4672, 4673, 4680, 4681);

const float tint_darken_cutoff = 0.7f;
const float tint_darken_offset = 1f - 0.7f;
const float tint_darken_factor = 1f / tint_darken_offset;

//public static final int BLOCK_SIZE = 16;
//
//public static final int INT_SIZE = 4;
//public static final int SCHEMATIC_SIZE = INT_SIZE * (BLOCK_SIZE * BLOCK_SIZE * BLOCK_SIZE);

int RAY_ITERATION_COUNT = 100;
bool ray_iteration_bound_reached = false;

int result_block_id = -1;
vec3 result_tint_color = vec3(1f);
int ph_result_sky_brightness = 0;

ivec3 ray_target = ivec3(-9999);
ivec3 ray_constraint = ivec3(-9999);
bool breakOnEmpty = false;

vec3 lightEmittance = vec3(0.0f);

#include "/photonics/modifiers/voxel_color_modifier.glsl"

void trace_ray(inout RayJob job, bool transparency) {
    job.direction = normalize(ph_signed_nudge(job.direction));

    vec3 direction_inv = 1.0f / job.direction;
    float t0 = ph_intersects_world(direction_inv, 16.0f * job.origin);
    if (t0 == -1.0f) {
        job.result_position = vec3(47823934.0f) - world_offset;
        return;
    }

    vec3 position = job.origin * 16.0f + (t0 + 0.03f) * job.direction;
    //    vec3 position = 16.0f * job.origin;

    vec3 ray_direction_sign = sign(job.direction);

    int emission_ptr = -1;
    ivec3 intersection_index = ivec3(7.5f * ray_direction_sign + vec3(7.5f, 12.5f, 17.5f));
    vec3 skipDelta = ray_direction_sign * 0.00001f;
    ivec3 intersection_offset = max(ivec3(ray_direction_sign), 0);

    int t_min = -1;

    int block_index = -1;

    int voxel_index = 0;


    ivec3 new_index = ivec3(0);
    ivec3 old_index = ivec3(0);

    ivec3 entries = ivec3(0);

    result_tint_color = vec3(1f);
    result_block_id = -1;

    ph_result_sky_brightness = 0;

    vec3 previous_tint = vec3(-1f);

    for (int i = RAY_ITERATION_COUNT; !(ray_iteration_bound_reached = i < 0); i--) {
        ivec3 w = ivec3(position); // TODO: maybe use uvec3, no negative check neccessary
        ivec3 block_position = w >> 4;

        if (!ph_is_inside(position)) // outside of world?
            return;
        if ((job.result_hit = block_position == ray_target)) { // ray target reached?
            break;
        }
        if (ray_constraint != ivec3(-9999) && block_position != ray_constraint)
            return;

        int scale = 0;
        int entry = 0;

        new_index.x = ph_get_world_index((w >> 8) & 31);
        if (new_index.x != old_index.x) { // TODO: CRITICAL! UBOs are too tiny for 32x32x32
            entries.x = root_array[new_index.x];
        }

        if (entries.x < 0) { // found chunk?
            ivec3 block_pos = (w >> 4) & 15;
            new_index.y = -entries.x + ph_get_index(block_pos);

            if (new_index.y != old_index.y) {
                entries.y = cb_array[new_index.y];
                if (entries.y < 0) { // found block
                    int block_data = -entries.y;

                    block_index = block_data & 0x1fff;
                    block_index = block_index * (ph_byte_size / 4);

                    ph_result_sky_brightness = block_data >> 13;

                    result_block_id = cb_array[block_index];
                    emission_ptr = block_index + 1;

                    voxel_index = block_index + 2;
                } else {
                    block_index = -1;
                }
            }

            if (block_index != -1) { // found block
                ivec3 voxel_pos = w & 15;
                new_index.z = voxel_index + ph_get_index(voxel_pos);
                if (new_index.z != old_index.z) {
                    entries.z = cb_array[new_index.z];

                    if (breakOnEmpty && entries.z == 519536640) {
                        job.result_hit = true;
                        entries.z = -0;

                        break;
                    }
                }

                if (entries.z < 0) { // found bloxel
                    #ifdef PH_USE_TRANSPARENCY
                    if (!transparency || (-entries.z & 0x7f000000) == 0) {
                        job.result_hit = true;
                        break;
                    } else {
                        vec4 color = ph_unpack_color(-entries.z);
                        #ifndef PH_VOXEL_COLOR_MODIFIER_DISABLED
                        voxel_color_modifier(color, position, block_position);
                        #endif

                        if (previous_tint != color.rgb) {
                            #if defined PH_USE_CUSTOM_ALPHA
                            result_tint_color*= PH_ALPHA_FUNC(color);
                            #else
                            result_tint_color*= mix(
                                color.rgb,
                                vec3(step(color.a, 0.5)),
                                abs((color.a * 2f) - 1f)
                            );
                            #endif

                            previous_tint = color.rgb;
                        }

                        #ifdef PH_FULL_TRANSPARENCY
                        scale = 0;
                        entry = ph_to_fake_air_entry(voxel_pos);
                        #else
                        scale = 4;
                        entry = ph_to_fake_air_entry(block_pos);
                        #endif
                    }
                    #else
                    job.result_hit = true;
                    break;
                    #endif
                } else { scale = 0; entry = entries.z; }
            } else { scale = 4; entry = entries.y; }
        } else { scale = 8; entry = entries.x; }

        old_index = new_index;

        ivec3 intersection = (((ivec3(entry) >> intersection_index) & 31) + intersection_offset) << scale;

        scale += 4;
        scale += int(scale == 8 + 4);
        intersection += w & (-1 << scale);

        vec3 t = (intersection - position) * direction_inv;
        t_min = int(t.x >= t.y);
        t_min = t.z < t[t_min] ? 2 : t_min;

        position += t[t_min] * job.direction;

        // "push precision" into lower decimal values to fight rounding errors
        position[t_min] = (intersection[t_min] * 0.01f + skipDelta[t_min]) * 100.0f;
    }

    if (job.result_hit) {
        lightEmittance = unpackUnorm4x8(cb_array[emission_ptr]).xyz;
    } else {
        lightEmittance = vec3(0f);
    }

    job.result_color = vec3((-entries.z >> 0) & 0xff, (-entries.z >> 8) & 0xff, (-entries.z >> 16) & 0xff) / 0xff;
    job.result_position = job.result_hit ? vec3(position / 16.0f) : (vec3(47823934.0f) - world_offset);

    if (t_min != -1)
        job.result_normal[t_min] = sign(-ray_direction_sign[t_min]);

    ray_target = ivec3(-1);
}

void trace_ray(inout RayJob job) { trace_ray(job, false); }

int get_block_pointer(vec3 position) {
    if (!ph_is_inside(16.0f * position)) {
        return -1;
    }

    ivec3 w = ivec3(position);
    int index = 0;

    index = root_array[ph_get_world_index((w >> 4) & 31)];
    if (index >= 0) return -1;

    index = cb_array[-index + ph_get_index(w & 15)];
    if (index >= 0) return -1;

    return (-index & 0xfff) * (ph_byte_size / 4);
}

int get_block_id(vec3 pos) {
    int ptr = get_block_pointer(pos);
    if (ptr == -1) {
        #if defined PH_USE_CUSTOM_AIR_ID
        return PH_AIR_ID;
        #else
        return -1;
        #endif
    }

    return cb_array[ptr];
}

// TODO: calculate normals; normals are always (0, 1, 0) at first iteration
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

bool ph_is_inside(vec3 position) {
    vec3 s = step(world_min_voxel, position) - step(world_max_voxel, position);
    return bool(s.x * s.y * s.z);
}

int ph_get_index(ivec3 position) {
    return morton[position.x] | (morton[position.y] << 1) | (morton[position.z] << 2);
}

int ph_get_world_index(ivec3 position) {
    return morton[position.x] | (morton[position.y] << 1) | (morton[position.z] << 2);
}

int get_result_sky_light(vec3 normal) {
    int index = clamp(
        int(
                abs(normal.x)*(normal.x*0.5+0.5)
                + abs(normal.y)*(normal.y*0.5+2.5)
                + abs(normal.z)*(normal.z*0.5+4.5)
                + 0.5),
        0,
        5
    );

    int level = (ph_result_sky_brightness >> (index * 3)) & 0x7;

    return int((level / 7f) * 15f);
}

vec4 ph_unpack_color(int packedColor) {
    vec3 rgb = vec3(
        (packedColor >> 0) & 0xff,
        (packedColor >> 8) & 0xff,
        (packedColor >> 16) & 0xff
    ) / 255f;

    return vec4(
        rgb,
        // Alpha uses only 7 bits
        (127 - (packedColor >> 24)) / 127f
    );
}

int ph_to_fake_air_entry(ivec3 pos) {
    int value = (pos.x << 0) |(pos.y << 5) | (pos.z << 10);

    return value | (value << 15);
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

#endif // PH_RAYTRACING