#file "/photonics/modifiers/restir_gi_modifier.glsl"
#create

void modify_restir_gi(inout vec3 color) {
    color = color * 0.364f * BOUNCED_LIGHT_I;
}