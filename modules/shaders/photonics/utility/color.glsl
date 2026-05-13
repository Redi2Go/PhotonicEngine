#ifndef PH_COLOR_UTIL_INCLUDE
#define PH_COLOR_UTIL_INCLUDE

const uvec4 ph_int_color_shift = uvec4(16, 8, 0, 24);

uvec4 ph_unpack_int_color(uint color) {
    return (uvec4(color) >> ph_int_color_shift) & 0xffu;
}

uint ph_pack_int_color(uvec4 color) {
    color <<= ph_int_color_shift;
    return color.x | color.y | color.z | color.w;
}

uvec4 ph_apply_int_tint(uvec4 color, uvec4 tint) {
    return ((color + 1) * tint) >> 8;
}

float ph_luminance(vec3 rgb) {
    return dot(rgb, vec3(0.2126f, 0.7152f, 0.0722f));
}
#endif