#version 430

layout(location = 0) in vec3 position;

uniform mat4 direction_transformation_matrix_in;
uniform mat4 gbufferModelViewInverse;
uniform vec3 sunPosition;
uniform vec3 upPosition;

out vec4 direction_vert_out;
flat out vec3 sunVecWorld;
flat out vec3 sunVec;
flat out vec3 upVec;

void main() {
    gl_Position = vec4(2.0f * position - 1.0f, 1.0f);

    // vertex_coord_in.z is -1; normally it is 1
    direction_vert_out = direction_transformation_matrix_in * vec4(2.0f * position - 1.0f, 1.0f);
    direction_vert_out.w = 1.0f / direction_vert_out.w;
    direction_vert_out.xyz *= direction_vert_out.w;

    direction_vert_out.xyz = normalize(direction_vert_out.xyz);

    sunVec = normalize(sunPosition);
    sunVecWorld = normalize(mat3(gbufferModelViewInverse) * sunVec);
    upVec = normalize(upPosition);
}
