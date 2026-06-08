#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = RESTIR_LIGHTING_SAMPLES_OUT) out float samples_frag_out;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;
    
    SampleHistory smple;
    sample_history_reproject(smple);
    
    samples_frag_out = smple.lighting.a;
}
