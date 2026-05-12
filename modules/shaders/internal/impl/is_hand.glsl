#include "/photonics/modifiers/is_hand_modifier.glsl"

#ifdef PH_IS_HAND_MODIFIER_DISABLED
bool ph_is_hand() {
    return texelFetch(depthtex0, ivec2(gl_FragCoord.xy), 0).x < 0.56;
}
#else
#define ph_is_hand modify_is_hand
#endif
