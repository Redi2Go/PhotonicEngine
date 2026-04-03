#file "/program/final.glsl"

#replace "const int colortex9Format = RGB16F; //colored light"
const int colortex9Format = RGB16F; //colored light
const int gaux5Format = RGBA8; // Photonics: albedo
const int gaux6Format = RGBA8; // Photonics: normal

#ifndef PH_RESTIR_COMBINED_GI
const int colortex12Format = RGBA16F; // Photonics: indirect
#endif
#endreplace