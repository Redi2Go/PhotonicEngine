uniform sampler2D fast_frag_data;
uniform sampler2D prev_fast_frag_data;

struct FastFrag {
    float depth;
    uint packed_normals;
};

void fast_frag_encode(float linear_depth, vec3 geo_normal, vec3 tex_normal, out vec2 value) {
    value.x = linear_depth;
    value.y = uintBitsToFloat(
        packUnorm4x8(
                vec4(
                        ph_encode_normal(geo_normal),
                        ph_encode_normal(tex_normal)
                )
        )
    );
}

void fast_frag_decode(out FastFrag frag, vec2 data) {
    frag.depth = data.x;
    frag.packed_normals = floatBitsToUint(data.y);
}

FastFrag fast_frag_fetch(ivec2 texel) {
    FastFrag result;
    fast_frag_decode(result, texelFetch(fast_frag_data, texel, 0).xy);

    return result;
}

FastFrag fast_frag_fetch_previous(ivec2 texel) {
    FastFrag result;
    fast_frag_decode(result, texelFetch(prev_fast_frag_data, texel, 0).xy);

    return result;
}

bool fast_frag_in_world(FastFrag frag) {
    return !isinf(frag.depth);
}

vec3 fast_frag_geo_normal(FastFrag frag) {
    return ph_decode_normal(unpackUnorm4x8(frag.packed_normals).xy);
}

vec3 fast_frag_tex_normal(FastFrag frag) {
    return ph_decode_normal(unpackUnorm4x8(frag.packed_normals).zw);
}
