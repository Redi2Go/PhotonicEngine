#include "/photonics/palette.glsl"
#include "/photonics/utility/normal_encoding.glsl"

#include "/photonics/internal/tracing/common.glsl"

#define PH_RAY_STATE_READY 0
#define PH_RAY_STATE_HAS_HIT 1
#define PH_RAY_STATE_HAS_MISS 2
#define PH_RAY_STATE_OUT_OF_BOUNDS 3

#define PH_RAY_DEFAULT_ITERATIONS 100
const float ph_16_rcp = 1.0f / 16.0f;

struct RayIterator {
    vec3 position;
    vec3 direction;
    int iterations;

    RayResult hit;
    int state; // this could improved, do I care? no
};

void _ray_iter_setup(inout RayIterator ray, vec3 direction) {
    ray.direction = normalize(ph_signed_nudge(direction));
    vec3 direction_inv = 1.0f / ray.direction;
    float t0 = ph_intersects_world(direction_inv, ray.position);
    if (t0 == -1) {
        ray.state = PH_RAY_STATE_OUT_OF_BOUNDS;
        return;
    }

    ray.position+= (t0 + 0.03f) * ray.direction;
    ray.state = PH_RAY_STATE_READY;
}

void ray_iter_begin(out RayIterator ray, vec3 position, vec3 direction) {
    ray.position = position * 16.0f;
    _ray_iter_setup(ray, direction);

    ray.iterations = 100;
}

void ray_iter_set_position(inout RayIterator ray, vec3 position) {
    ray.position = position * 16.0f;
}

void ray_iter_set_direction(inout RayIterator ray, vec3 direction) {
    _ray_iter_setup(ray, direction);
}

vec3 ph_trace_direction_inv = vec3(0.0f);
ivec3 ph_trace_intersection_index = ivec3(0);
ivec3 ph_trace_intersection_offset = ivec3(0);
vec3 ph_trace_skip_delta = vec3(0.0f);

int ph_trace_t_min = -1;
int ph_trace_depth = 0;
ivec3 ph_trace_ipos = ivec3(0);

ivec3 _ray_intersection(uint entry, int index_shift_factor) {
    ivec3 intersection = (((ivec3(entry) >> ph_trace_intersection_index) & 15) + ph_trace_intersection_offset) << index_shift_factor;
    intersection += ph_trace_ipos & (-1 << (index_shift_factor + 4));

    return intersection;
}

bool _ray_skip_intersection(inout RayIterator ray, ivec3 intersection) {
    vec3 t = intersection - ray.position;

    t*= ph_trace_direction_inv;

    ph_trace_t_min = int(t.x >= t.y);
    ph_trace_t_min = t.z < t[ph_trace_t_min] ? 2 : ph_trace_t_min;

    ray.position += t[ph_trace_t_min] * ray.direction;

    // "push precision" into lower decimal values to fight rounding errors
    ray.position[ph_trace_t_min] = (intersection[ph_trace_t_min] * 0.01f + ph_trace_skip_delta[ph_trace_t_min]) * 100.0f;
    if (!ph_is_inside(ray.position)) {
        ray.state = PH_RAY_STATE_OUT_OF_BOUNDS;
        ray.hit = ph_ray_miss;

        return true;
    }

    ivec3 new_ipos = ivec3(ray.position);
    ivec3 diff = (ph_trace_ipos ^ new_ipos);

    ph_trace_depth = findMSB(diff.x | diff.y | diff.z) >> 2;
    if (ph_trace_depth > PH_LAST_INDEX) {
        ray.state = PH_RAY_STATE_OUT_OF_BOUNDS;
        ray.hit = ph_ray_miss;

        return true;
    }

    ph_trace_ipos = new_ipos;
    return false;
}

const ivec3 ph_ray_no_target = ivec3(-1);
void _ray_iter_trace_next(inout RayIterator ray, ivec3 target) {
    if (ray.state != PH_RAY_STATE_READY) return;
    if (ray.iterations == 0) return;

    ph_trace_direction_inv = 1.0f / ray.direction;
    vec3 ray_direction_sign = sign(ray.direction);

    ph_trace_intersection_index = ivec3(7.5f * ray_direction_sign + vec3(7.5f, 12.5f, 17.5f));
    ph_trace_intersection_offset = max(ivec3(ray_direction_sign), 0);
    ph_trace_skip_delta = ray_direction_sign * 0.00001f;


    ph_trace_t_min = -1;

    uint[PH_WORLD_DEPTH + 1] entry_ptrs = uint[PH_WORLD_DEPTH + 1](0);

    uint block_header_ptr = 0;
    uint block_palette_size = 0;

    ph_trace_depth = PH_LAST_INDEX;
    ph_trace_ipos = ivec3(ray.position);

    for (; ray.iterations > 0; ray.iterations--) {
        int index_shift_factor = ph_trace_depth << 2;
        ivec3 voxel_pos = ph_trace_ipos >> index_shift_factor;
        int index = ph_get_voxel_index(voxel_pos & 15);

        uint parent_voxel = entry_ptrs[ph_trace_depth + 1];
        uint entry = ph_world_buffer[parent_voxel + index];

        if (ph_is_data(entry)) {
            uint ptr = entry & ~PH_SIGN_BIT;

            if (ph_trace_depth == PH_BLOCK_DEPTH) {
                vec3 normal = vec3(0f);
                normal[ph_trace_t_min] = -ray_direction_sign[ph_trace_t_min];

                // the 1 bit of a block voxel entry is used to store transparency,
                // but we need to double it anyway as each palette entry is 2 ints (1 tint, 1 palette ptr)
                // so we can just remove the transparency bit and use the raw value
                // We also use +3 instead of +5 since 0 means empty, index 0 becomes 1 (2 since its doubled)
                uint palette_header_ptr = block_header_ptr + ((ptr & ~1u));

                ray.hit = new_ray_result(
                    ray.position * ph_16_rcp,
                    ph_encode_voxel_normal(normal),
                    palette_header_ptr,
                    (ptr & 1u) == 1
                );

                ray.state = PH_RAY_STATE_HAS_HIT;
                return;
            } else if (ph_trace_depth == PH_CHUNK_DEPTH) {
                if (voxel_pos == target) {
                    ray.hit = new_ray_result(
                        vec3(target),
                        0,
                        1,
                        false
                    );

                    ray.state = PH_RAY_STATE_HAS_HIT;
                    return;
                }

                //Block header:
                //ptr + 0 = block voxel ptr
                //ptr + 1 = palette size

                block_header_ptr = ptr;

                entry_ptrs[ph_trace_depth] = ph_world_buffer[block_header_ptr + 0];
                block_palette_size = ph_world_buffer[block_header_ptr + 1];
            } else entry_ptrs[ph_trace_depth] = ptr;

            ph_trace_depth--;
            continue;
        }

        // Found air!
        ivec3 intersection = _ray_intersection(entry, index_shift_factor);
        if (_ray_skip_intersection(ray, intersection))
            return;
    }

    ray.state = PH_RAY_STATE_HAS_MISS;
    ray.hit = ph_ray_miss;
}

bool ray_iter_has_next(inout RayIterator ray) {
    _ray_iter_trace_next(ray, ph_ray_no_target);
    return ray.state == PH_RAY_STATE_HAS_HIT;
}

RayResult ray_iter_next(inout RayIterator ray) {
    _ray_iter_trace_next(ray, ph_ray_no_target);
    if (ray.state != PH_RAY_STATE_OUT_OF_BOUNDS)
        ray.state = PH_RAY_STATE_READY;

    return ray.hit;
}

bool ray_iter_has_next_block(inout RayIterator ray, vec3 target) {
    _ray_iter_trace_next(ray, ivec3(target));
    return ray.state == PH_RAY_STATE_HAS_HIT;
}

RayResult ray_iter_next_block(inout RayIterator ray, vec3 target) {
    _ray_iter_trace_next(ray, ivec3(target));
    if (ray.state != PH_RAY_STATE_OUT_OF_BOUNDS)
    ray.state = PH_RAY_STATE_READY;

    return ray.hit;
}

void _ray_iter_skip_scale(inout RayIterator ray, int depth) {
    if (ray.state == PH_RAY_STATE_OUT_OF_BOUNDS) return;

    int index_shift_factor = depth << 2;
    ivec3 pos = (ph_trace_ipos >> index_shift_factor) & 15;

    ivec3 intersection = _ray_intersection(ph_to_fake_air_entry(pos), index_shift_factor);
    _ray_skip_intersection(ray, intersection);
}

void ray_iter_skip_block(inout RayIterator ray) {
    _ray_iter_skip_scale(ray, PH_CHUNK_DEPTH);
}

void ray_iter_skip_voxel(inout RayIterator ray) {
    _ray_iter_skip_scale(ray, PH_BLOCK_DEPTH);
}

bool ray_iter_is_in_bounds(RayIterator ray) {
    return ray.state != PH_RAY_STATE_OUT_OF_BOUNDS;
}

void ray_iter_apply_transparency(inout vec4 accumulator, vec4 albedo) {
    if (accumulator.a != 0) {
        float mix_factor = (1 - accumulator.a) * albedo.a;
        accumulator.rgb = mix(accumulator.rgb, albedo.rgb, mix_factor);
        accumulator.a+= mix_factor;
    } else accumulator = albedo;
}