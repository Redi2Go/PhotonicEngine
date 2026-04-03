#file "/program/gbuffers_voxels.glsl"
#template "/program/gbuffers_terrain.glsl"

#replace "#include "/lib/settings.glsl""
#include "/lib/settings.glsl"

#undef PARALLAX
#undef DYNAMIC_HANDLIGHT
#undef REFLECTION_RAIN
#undef EMISSIVE_RECOLOR
#undef NORMAL_PLANTS
#undef SELF_SHADOW
#undef SPECULAR_HIGHLIGHT_ROUGH
#undef REFLECTION_ROUGH
#endreplace

#replace "vec3 lightVec = sunVec * ((timeAngle < 0.5325 || timeAngle > 0.9675) ? 1.0 : -1.0);"

vec3 lightVec = sunVec * ((timeAngle < 0.5325 || timeAngle > 0.9675) ? 1.0 : -1.0);
uniform mat4 gbufferProjection; //, gbufferModelView;
uniform float frameTime;
#include "/photonics/photonics.glsl"

#endreplace

#replace "vec4 albedo = texture2D(texture, texCoord) * vec4(color.rgb, 1.0);"
    vec3 screenPos = vec3(gl_FragCoord.xy / vec2(viewWidth, viewHeight), gl_FragCoord.z);
    #ifdef TAA
    vec3 viewPos = ToNDC(vec3(TAAJitter(screenPos.xy, -0.5), screenPos.z));
    #else
    vec3 viewPos = ToNDC(screenPos);
    #endif
    vec3 worldPos = ToWorld(viewPos);

    RayJob ray = RayJob(vec3(0), vec3(0), vec3(0), vec3(0), vec3(0), false);
    ray.origin = worldPos + cameraPosition - world_offset - 0.001f * blockNormal;
    ray.direction = worldPos - gbufferModelViewInverse[3].xyz;
    ray_constraint = ivec3(ray.origin);
    trace_ray(ray);

    if (!ray.result_hit) {
        discard;
    }

    if (ray.result_normal == vec3(0.0f)) {
        ray.result_normal = blockNormal;
    }

    worldPos = ray.result_position + world_offset - cameraPosition;
    viewPos = (gbufferModelView * vec4(worldPos, 1.0f)).xyz;
    vec4 ndc4 = gbufferProjection * vec4(viewPos, 1.0f);
    screenPos = ndc4.xyz / ndc4.w * 0.5f + 0.5f;
    gl_FragDepth = screenPos.z;

    vec3 normal = normalize(gl_NormalMatrix * ray.result_normal);
    vec4 albedo = vec4(ray.result_color, 1.0f);

    vec3 oldAlbedo = albedo.xyz;
#endreplace

#replace "/* DRAWBUFFERS:08367 */"
    /* RENDERTARGETS:0,8,3,6,7,10,11 */
    gl_FragData[5] = vec4(oldAlbedo, 1.0f);
    gl_FragData[6] = vec4(0.5f * normal + 0.5f, 1.0f);
#endreplace

#replace "/* DRAWBUFFERS:0367 */"
    /* RENDERTARGETS:0,3,6,7,10,11 */
    gl_FragData[4] = vec4(oldAlbedo, 1.0f);
    gl_FragData[5] = vec4(0.5f * normal + 0.5f, 1.0f);
#endreplace

#replace "varying vec3 normal;"
varying vec3 normal;
varying vec3 blockNormal;
#endreplace

#replace "normal = normalize(gl_NormalMatrix * gl_Normal);"
    normal = normalize(gl_NormalMatrix * gl_Normal);
    blockNormal = gl_Normal;
#endreplace