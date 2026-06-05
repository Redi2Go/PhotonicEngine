#version 430

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir/restir.glsl"

layout(location = RESTIR_LIGHTING_SAMPLES_OUT) out float samples_frag_out;

void main() {
    if (!prepare_frag(0)) return;
    
    SampleHistory smple;
    sample_history_reproject(smple);
    
    samples_frag_out = smple.lighting.a;
}
