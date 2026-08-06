// Deprecated: Remove for 0.4 release

#if !defined PH_DEPTH_FETCH
#include "/photonics/modifiers/restir_denoiser_depth_fetch_modifier.glsl"

#ifdef PH_RESTIR_DENOISER_DEPTH_FETCH_MODIFIER_DISABLED
#define DEPTH_MODIFIER(p) p
#else
#define DEPTH_MODIFIER(p) modify_denoiser_depth_fetch(p)
#endif

//ph_required: uniform sampler2D depthtex0;
float load_depth() {
    return texelFetch(depthtex0, DEPTH_MODIFIER(ivec2(gl_FragCoord.xy)), 0).r;
}
#endif
