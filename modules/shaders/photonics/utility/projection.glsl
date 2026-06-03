#ifndef PH_PROJECTION_UTILITY_INCLUDE
#define PH_PROJECTION_UTILITY_INCLUDE

vec4 ph_project(mat4 m, vec3 pos) {
    return vec4(m[0].x, m[1].y, m[2].zw) * pos.xyzz + m[3];
}

vec3 ph_transform(mat4 m, vec3 pos) {
    return mat3(m) * pos + m[3].xyz;
}

vec3 ph_project_and_divide(mat4 m, vec3 pos) {
    vec4 homogenous = ph_project(m, pos);
    return homogenous.xyz / homogenous.w;
}

vec3 ph_reproject_player_pos(vec3 player_pos, bool hand, vec2 jitter, out vec3 previous_player_pos) {
    vec3 camera_offset = hand ? vec3(0.0f) : cameraPosition - previousCameraPosition;
    previous_player_pos = player_pos + camera_offset;

    vec3 previous_pos = ph_transform(gbufferPreviousModelView, previous_player_pos);
    previous_pos = ph_project_and_divide(gbufferPreviousProjection, previous_pos);
    previous_pos.xy+= jitter;

    return (previous_pos) * 0.5 + 0.5f;
}

#endif
