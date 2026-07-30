#file "/photonics/modifiers/is_hand_modifier.glsl"
#create

bool modify_is_hand() {
    return texelFetch(depthtex1, ivec2(gl_FragCoord.xy), 0).x < 0.56;
}