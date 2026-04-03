#file "/program/gbuffers_voxels.vsh"
#template "/program/gbuffers_all_solid.vsh"

#replace "out vec4 tint;"
out vec4 tint;
out vec3 block_normal;
#endreplace

#replace "tint = gl_Color;"
tint = gl_Color;
block_normal = gl_Normal;
#endreplace