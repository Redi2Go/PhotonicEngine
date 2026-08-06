#version 430

layout(location = 0) in vec3 position;

void main() {
    gl_Position = vec4(2.0f * position - 1.0f, 1.0f);
}
