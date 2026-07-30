#file "/program/shadow_voxels.glsl"
#create

#include "/lib/settings.glsl"

#ifdef FSH

in vec3 worldPos;
in vec3 cageNormal;

uniform int frameCounter, frameTime;
uniform float viewWidth, viewHeight;

uniform vec3 cameraPosition;

uniform mat4 shadowProjection, shadowProjectionInverse;
uniform mat4 shadowModelView, shadowModelViewInverse;

uniform sampler2D depthtex0;

#include "/photonics/photonics.glsl"

void main() {
    RayJob ray = RayJob(vec3(0), vec3(0), vec3(0), vec3(0), vec3(0), false);
    ray.origin = worldPos - world_offset - 0.01f * cageNormal;
    ray.direction = mat3(shadowModelViewInverse) * vec3(0.0f, 0.0f, -1.0f);
    ray_constraint = ivec3(ray.origin);
    trace_ray(ray);

    if (!ray.result_hit) {
        discard;
    }

    {	// Calculate depth position
        vec3 feetPlayerPos = (ray.result_position + world_offset) - cameraPosition;
        vec3 shadowViewPos = (shadowModelView * vec4(feetPlayerPos, 1.0f)).xyz;
        vec4 shadowNdcPos = shadowProjection * vec4(shadowViewPos, 1.0f);
        shadowNdcPos.xyz /= shadowNdcPos.w;

        shadowNdcPos.z *= 0.2f; // distortion

        vec3 shadowScreenPos = shadowNdcPos.xyz * 0.5f + 0.5f;

        gl_FragDepth = shadowScreenPos.z;
    }

    vec4 albedo = vec4(ray.result_color, 1.0f);
    gl_FragData[0] = albedo;
}

#endif

#ifdef VSH

out vec3 worldPos;
out vec3 cageNormal;

uniform mat4 shadowProjection, shadowProjectionInverse;
uniform mat4 shadowModelView, shadowModelViewInverse;
uniform vec3 cameraPosition;

void main() {
    cageNormal = gl_Normal;

    vec4 position = shadowModelViewInverse * shadowProjectionInverse * ftransform();

    worldPos = position.xyz + cameraPosition.xyz;

    gl_Position = shadowProjection * shadowModelView * position;

    float dist = sqrt(gl_Position.x * gl_Position.x + gl_Position.y * gl_Position.y);
    float distortFactor = dist * shadowMapBias + (1.0 - shadowMapBias);

    gl_Position.xy *= 1.0 / distortFactor;
    gl_Position.z = gl_Position.z * 0.2;
}

#endif