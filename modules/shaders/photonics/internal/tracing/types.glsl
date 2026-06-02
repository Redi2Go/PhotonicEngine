// TODO support without int64
#extension GL_ARB_gpu_shader_int64 : require

uint ph_get_node_cell_index(vec3 pos, int scale) {
    uvec3 cell_pos = (floatBitsToUint(pos) >> scale) & 3u;
    return cell_pos.x + (cell_pos.z << 2) + (cell_pos.y << 4);
}

vec3 ph_floor_scale(vec3 pos, int scale) {
    uint mask = ~0u << scale;
    return uintBitsToFloat(floatBitsToUint(pos) & mask);
}

uint ph_bitCount_64(uint64_t mask, uint width) {
    uvec2 low_high = unpackUint2x32(mask);
    uint mask_32 = low_high[0];

    uint count = 0;

    if (width >= 32) {
        count = bitCount(mask_32);
        mask_32 = low_high[1];
    }

    uint m = 1u << (width & 31u);
    count+= bitCount(mask_32 & (m - 1u));
    return count;
}

vec3 ph_get_mirrored_pos(vec3 pos, vec3 dir, bool range_check) {
    vec3 mirrored = uintBitsToFloat(floatBitsToUint(pos) ^ 0x7FFFFFu);

    if (range_check && (any(lessThan(pos, vec3(1.0f))) || any(greaterThanEqual(pos, vec3(2.0f)))))
        mirrored = 3.0 - pos;

    return vec3(
        dir.x > 0.0f ? mirrored.x : pos.x,
        dir.y > 0.0f ? mirrored.y : pos.y,
        dir.z > 0.0f ? mirrored.z : pos.z
    );
}

vec3 ph_to_norm_pos(vec3 position, vec3 ray_direction) {
    return ph_get_mirrored_pos(
        (position / world_tree_size) + 1.0f
        , ray_direction,
        true
    );
}

bool ph_is_target(vec3 pos, vec3 target) {
    return ph_floor_scale(pos, world_block_scale_exp) == target;
}


struct RtNode {
    uint data0;
    uint64_t child_mask;
};

bool rt_node_is_leaf(RtNode node) {
    return (node.data0 & 1u) != 0;
}

uint rt_node_child_ptr(RtNode node) {
    return node.data0 >> 1u;
}

bool rt_node_has_child(RtNode node, uint child_index) {
    const uint64_t zero_64 = uint64_t(0u);
    const uint64_t one_64 = uint64_t(1u);

    return ((node.child_mask >> child_index) & one_64) != zero_64;
}

uint rt_node_get_child(RtNode node, uint child_index, int scale_exp) {
    uint multiplier = scale_exp == world_block_scale_exp ? 4u : 3u;

    return rt_node_child_ptr(node) + (ph_bitCount_64(node.child_mask, child_index) * multiplier);
}

RtNode load_rt_node(uint index) {
    return RtNode(
        ph_world_buffer[index],
        packUint2x32(
            uvec2(
                ph_world_buffer[index + 1],
                ph_world_buffer[index + 2]
            )
        )
    );
}

struct LeafNode {
    uint data0;
};

bool leaf_node_is_transparent(LeafNode node) {
    return (node.data0 & 1u) != 0;
}

uint leaf_node_palette_entry(LeafNode node) {
    return node.data0 >> 1u;
}

LeafNode load_leaf_node(RtNode node, uint child_index) {
    return LeafNode(ph_world_buffer[rt_node_child_ptr(node) + ph_bitCount_64(node.child_mask, child_index)]);
}