#file "/program/gbuffers_entities.glsl"

#replace "float skyOcclusion = 0.0;"
float skyOcclusion = 0.0;
vec3 oldAlbedo = albedo.xyz;
#endreplace

#replace "/* DRAWBUFFERS:018367 */"
/* RENDERTARGETS:0,1,8,3,6,7,10,11 */
gl_FragData[6] = vec4(oldAlbedo, 1.0f);
gl_FragData[7] = vec4(0.5f * normal + 0.5f, 1.0f);
#endreplace

#replace "/* DRAWBUFFERS:01367 */"
/* RENDERTARGETS:0,1,3,6,7,10,11 */
gl_FragData[5] = vec4(oldAlbedo, 1.0f);
gl_FragData[6] = vec4(0.5f * normal + 0.5f, 1.0f);
#endreplace
