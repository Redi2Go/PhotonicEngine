#version 330 core

//layout(location = 0) in vec3 iris_Position;
//layout(location = 1) in vec4 iris_Color;
//layout(location = 2) in vec2 iris_UV0;
//layout(location = 3) in ivec2 iris_UV1;
//layout(location = 4) in ivec2 iris_UV2;
//layout(location = 5) in vec3 iris_Normal;
//layout(location = 7) in ivec3 iris_Entity;

in vec3 iris_Position;
in vec3 iris_Normal;
in vec4 iris_Color;
in ivec2 iris_UV2;
in vec2 iris_UV0;
in ivec3 iris_Entity;
in ivec2 iris_UV1;
in vec4 mc_Entity;
in vec4 mc_midTexCoord;
in vec4 at_tangent;

uniform int RenderIndex;

out vec2 texCoord0;
out vec3 normal;
out vec3 pos;
out vec4 tint;

// TODO: D.R.Y.
void main() {
//    gl_Position = ProjMat * ModelViewMat * vec4(Position + ChunkOffset, 1.0);

//    vec3 iris_Position = ((_deinterleave_u20x3(a_Position) * VERTEX_SCALE) + VERTEX_OFFSET);
//    vec2 iris_UV0 = _get_texcoord();
//    vec4 iris_Color = a_Color;
//    vec3 iris_Normal = oct_to_vec3(iris_Normal.xy);

    pos = iris_Position * 16.0f - 0.5f * iris_Normal.xyz;

    vec2 transformed_position;
    vec3 transformed_normal;
    if (RenderIndex == 0) {
        transformed_position = iris_Position.xy;
        transformed_normal = iris_Normal.xyz;
    } else if (RenderIndex == 1) {
        transformed_position = iris_Position.xz;
        transformed_normal = iris_Normal.xzy;
    } else {
        transformed_position = iris_Position.zy;
        transformed_normal = iris_Normal.zyx;
    }

//    transformed_position = floor(transformed_position * 16.0f) / 16.0f + 0.5f;

    // TODO: 45° is not 0.5, it's cos(45°) ~ 0.71
    // TODO: BUG! Normals are (0,0,0) for minecraft:chest
    if (abs(dot(transformed_normal, vec3(0, 0, 1))) < 0.5f) {
        gl_Position = vec4(-999.0f, -999.0f, -999.0f, 0.0f);
        return;
    }

    gl_Position = vec4(transformed_position.xy * 2.0f - 1.0f, 0.0f, 1.0f);

    texCoord0 = iris_UV0;
    normal = iris_Normal;
    tint = iris_Color;
}
