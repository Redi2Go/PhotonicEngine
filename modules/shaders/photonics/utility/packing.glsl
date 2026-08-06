#ifndef PH_PACKING_UTIL_INCLUDE
#define PH_PACKING_UTIL_INCLUDE

uint packUnormR11G11B10(vec3 value) {
    const uint elevenBits = 2047;
    const uint tenBits = 1023;

    const vec3 scale = vec3(elevenBits, elevenBits, tenBits);
    const uvec3 mask = uvec3(elevenBits, elevenBits, tenBits);
    const uvec3 shift = uvec3(21, 10, 0);

    uvec3 result = (uvec3(clamp(value, 0.0f, 1.0f) * scale) & mask) << shift;
    return result.x | result.y | result.z;
}

vec3 unpackUnormR11G11B10(uint value) {
    const uint elevenBits = 2047;
    const uint tenBits = 1023;

    const vec3 rcpScale = 1.0f / vec3(elevenBits, elevenBits, tenBits);
    const uvec3 mask = uvec3(elevenBits, elevenBits, tenBits);
    const uvec3 shift = uvec3(21, 10, 0);

    return vec3((uvec3(value) >> shift) & mask) * rcpScale;
}

uint packSnormR11G11B10(vec3 value) {
    return packUnormR11G11B10(value * 0.5f + 0.5f);
}

vec3 unpackSnormR11G11B10(uint value) {
    return unpackUnormR11G11B10(value) * 2.0f - 1.0f;
}

#endif
