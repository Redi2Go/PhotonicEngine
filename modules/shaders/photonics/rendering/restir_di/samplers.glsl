uniform sampler2D restir_direct;

vec3 sample_photonics_direct(vec2 tex_coord) {
    return texture(restir_direct, tex_coord).rgb;
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
    return vec3(0f);
}