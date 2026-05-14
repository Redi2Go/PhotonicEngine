uniform sampler2D restir_lighting;

vec3 sample_photonics_direct(vec2 tex_coord) {
    vec4 lighting = texture2D(restir_lighting, tex_coord);
    return (lighting.rgb / max(lighting.a, 1f));
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
    return vec3(0f);
}