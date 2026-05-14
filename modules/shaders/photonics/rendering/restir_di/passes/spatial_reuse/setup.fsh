#version 430

layout(location = 5) out float samples_frag_out;

#include "/photonics/rendering/common.glsl"
#include "/photonics/rendering/restir_di/restir.glsl"

void main() {
    if (light_list_size == 0 || !prepare_frag(0)) return;
    
    SampleHistory smple;
    sample_history_reproject(smple);
    
    samples_frag_out = smple.lighting.a;
}