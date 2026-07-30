#file "/program/gbuffers_block.glsl"

#replace "float skyOcclusion = 0.0;"
float skyOcclusion = 0.0;
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