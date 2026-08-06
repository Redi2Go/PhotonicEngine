#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/direct/reservoir.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec3 di_reservoir_0;
layout(location = DIRECT_OUT) out vec3 di_output;

void main() {
    setup_frag_data(31);
    di_output = vec3(0.0f);

    if (!frag_is_in_world) return;

    float direct_sample_weight = 0.0f;

    DirectReservoir direct_result = direct_reservoir_empty();
    DirectReservoir reused_reservoir = direct_reservoir_empty();

    direct_reservoir_load(reused_reservoir, frag_tex_coord);
    direct_reservoir_merge(direct_result, reused_reservoir, direct_sample_weight);

//    #if PH_RESTIR_SPATIAL_REUSE_SAMPLES > 0
//    if (direct_reservoir_load_previous(reused_reservoir, frag_tex_coord, false)) {
//        direct_reservoir_validate_visiblity(reused_reservoir, frag_rt_pos);
//        direct_reservoir_merge(direct_result, reused_reservoir, direct_sample_weight);
//    }
//    #else
//    direct_reservoir_validate_visiblity(direct_result, frag_rt_pos);
//    #endif

    direct_reservoir_clamp_samples(direct_result);
    direct_reservoir_finalize_weight(direct_result, direct_sample_weight);

    di_output = direct_reservoir_get_final_color(direct_result, frag_rt_pos, frag_geo_normal, frag_tex_normal);
    di_output *= get_exposure();

    direct_reservoir_encode(direct_result, di_reservoir_0);
}
