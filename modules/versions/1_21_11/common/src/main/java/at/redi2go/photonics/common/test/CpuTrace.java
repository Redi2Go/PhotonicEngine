package at.redi2go.photonics.common.test;

import org.joml.Vector3f;

import java.nio.IntBuffer;

public class CpuTrace {
    private static final int BLOCK_DEPTH = 0;
    private static final int CHUNK_DEPTH = 1;

    private static final int SIGN_BIT = Integer.MIN_VALUE;

    private static boolean is_negative(ivec3 pos) {
        return ((pos.x() | pos.y() | pos.z()) & 0x80000000) != 0;
    }

    private static Vector3f sign(Vector3f value) {
        return new Vector3f(
                Math.signum(value.x),
                Math.signum(value.y),
                Math.signum(value.z)
        );
    }

    private static int ph_to_fake_air_entry(ivec3 pos) {
        int value = (pos.x()) |(pos.y() << 5) | (pos.z() << 10);

        return value | (value << 15);
    }

    private static boolean ph_is_data(int entry, int depth) {
        return (depth == 0 ? entry : entry & SIGN_BIT) != 0;
    }

    private static int sectionIndex(int realIndex, int shift) {
        return realIndex & ((1 << shift) - 1);
    }

    public static boolean trace(Vector3f ray_start, Vector3f ray_direction, IntBuffer buffer) {
        final int MAX_ITERATIONS = 400;

        final int LAST_INDEX = 2;

        ray_direction = ray_direction.normalize();

        Vector3f direction_inv = new Vector3f(1f).div(ray_direction);
        Vector3f position = ray_start.mul(16f, new Vector3f());
        Vector3f ray_direction_sign = sign(ray_direction);

        ivec3 intersection_index = new ivec3(new Vector3f(7.5f).mul(ray_direction_sign).add(new Vector3f(7.5f, 12.5f, 17.5f)));
        Vector3f skipDelta = ray_direction_sign.mul(0.00001f, new Vector3f());
        ivec3 intersection_offset = new ivec3(ray_direction_sign).max(0);

        int t_min = 1;
        int[] entry_ptrs = new int[LAST_INDEX + 2];

        int block_header_ptr = 0;
        int block_palette_size = 0;

        int data_shift = 0;
        int block_data_mask = ~0;

        int depth = LAST_INDEX;
        ivec3 ipos = new ivec3(position);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            if (depth > LAST_INDEX) break;

            int index_shift_factor = depth << 2;
            int index = ipos.shr(index_shift_factor).and(15).flatten();

            int parent_voxel = entry_ptrs[depth + 1];
            int entry = buffer.get(parent_voxel + (index >> data_shift));

            if (depth == BLOCK_DEPTH) {
                entry = entry >>> (sectionIndex(index, data_shift) << (5 - data_shift));
                entry &= block_data_mask;
            }

            if (ph_is_data(entry, depth)) {
                if (depth == BLOCK_DEPTH) return true;

                int ptr = entry & ~SIGN_BIT;
                if (depth == CHUNK_DEPTH) {
                    //Block header:
                    //ptr + 0 = skylight
                    //ptr + 1 = block voxel ptr
                    //ptr + 2 = data shift
                    //ptr + 3 = data mask
                    //ptr + 4 = palette size

                    block_header_ptr = ptr;

                    entry_ptrs[depth] = buffer.get(block_header_ptr + 1);
                    data_shift = buffer.get(block_header_ptr + 2);
                    block_data_mask = buffer.get(block_header_ptr + 3);
                    block_palette_size = buffer.get(block_header_ptr + 4);
                } else {
                    entry_ptrs[depth] = ptr;

                    data_shift = 0;
                    block_data_mask = ~0;
                }

                depth--;
                continue;
            } else if (depth == BLOCK_DEPTH) {
                entry = ph_to_fake_air_entry(ipos.shr(index_shift_factor).and(15));
            }

            // Found air!
            ivec3 intersection = new ivec3(entry)
                    .shr(intersection_index)
                    .and(15)
                    .plus(intersection_offset)
                    .shl(index_shift_factor);

            intersection = intersection.plus(ipos.and(-1 << (index_shift_factor + 4)));

            Vector3f t = intersection.toVec3().sub(position).mul(direction_inv);
            t_min = t.x >= t.y ? 1 : 0;
            t_min = t.z < t.get(t_min) ? 2 : t_min;

            position = position.add(ray_direction.mul(t.get(t_min), new Vector3f()));

            // "push precision" into lower decimal values to fight rounding errors
            position.setComponent(t_min, (intersection.get(t_min) * 0.01f + skipDelta.get(t_min)) * 100.0f);

            ivec3 new_ipos = new ivec3(position);
            if (is_negative(new_ipos)) return false;

            ivec3 diff = ipos.xor(new_ipos).shr(1);
            depth = (32 - Integer.numberOfLeadingZeros(diff.x() | diff.y() | diff.z())) / 4;

            ipos = new_ipos;
        }

        return false;
    }
}
