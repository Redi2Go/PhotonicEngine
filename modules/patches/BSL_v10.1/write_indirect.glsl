#file "/photonics/write_indirect.glsl"

#replace "void write_indirect(vec3 color);"
layout(location = 0) out vec4 fragColor;
void write_indirect(vec3 color) {
    /* RENDERTARGETS:12 */
    fragColor = vec4(color, 1.0f);
}
#endreplace