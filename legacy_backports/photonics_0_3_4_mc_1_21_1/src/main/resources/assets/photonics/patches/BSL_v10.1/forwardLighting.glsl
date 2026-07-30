#file "/lib/lighting/forwardLighting.glsl"

#replace "vec3 sceneLighting = mix(ambientCol * lightmap.y, lightCol, fullShadow * shadowMult);"
float dist = clamp(length(worldPos) * 0.005, 0.0f, 1.0f);
vec3 sceneLighting = mix(dist * ambientCol * lightmap.y, lightCol, fullShadow * shadowMult);
#endreplace

#replace "vec3 blockLighting = blocklightCol * newLightmap * newLightmap;"
vec3 blockLighting = vec3(0.0f);
#endreplace