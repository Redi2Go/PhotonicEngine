# Documentation for 0.4

Temporary documentation for 0.4 while its under development.

## General Changes

Photonics API is no longer a single file. 
Each component (light list, tracing, ect.) of its api is now split across multiple files.

shader_interface has now be split into 2 files: `lighting_interface.glsl` and `world_interface.glsl`.
Photonics now also works in player space instead of world space.


Like before all files included by the shader must have an empty stub.

All files are protected by #if guards. 
If a file returns a type from another file it will have included that file.

Some structs may be documented to have *no* members. 
This means their members are unstable and should not be manually accessed.

## `/photonics/light.glsl`

### `LIGHT_TYPE_INVALID`
### `LIGHT_TYPE_NOT_TRACED`
### `LIGHT_TYPE_TRACED`

### `Light`
```glsl
struct Light {
    int type; // One of LIGHT_TYPE_INVALID, LIGHT_TYPE_NOT_TRACED, or LIGHT_TYPE_TRACED
    int index; // Index of the light in the light list if >= 0, otherwise -1 for light in main hand & -2 for offhand
    int blockId; // The block ID of the light
    vec3 position; // The position of the light in RT space
    vec3 color; // The color of the light, multiplied by intensity. To get the original color do `light.color / light.intensity`1=
    float intensity; // The intensity of the light
    vec2 attenuation; // Attenuation factors of the light
    float falloff; // Falloff factor of the light
    float block_radius; // The radius of the light in blocks
};
```

### `get_main_hand_light`
```glsl
Light get_main_hand_light();
```
Returns the light in the main hand. The result of this method if `main_hand_has_light` is `false` is unspecified.

### `get_off_hand_light`
```glsl
Light get_main_hand_light();
```
Returns the light in the off hand. The result of this method if `off_hand_has_light` is `false` is unspecified.

### `light_is_valid`
```glsl
bool light_is_valid(Light light);
```
Returns `true` if `light` is valid.

### `light_sample_at`
```glsl
vec3 light_sample_at(
    Light light,
    vec3 sample_pos,
    vec3 source_pos,
    vec3 geometry_normal,
    vec3 texture_normal
);
```
Returns the light contribution at `sample_pos` for `light`.

## `/photonics/light_list.glsl`

```glsl
uniform int light_list_size;
```
The size of the light list.

### `light_list_get`
```glsl
Light light_list_get(int index);
```
Returns the light at `index` from the light list.

### `light_list_map_index`
```glsl
int light_list_map_index(int old_index);
```
Maps a light list index from the previous frame to the current frame. 
A value of -1 means the light was removed from the light list.


## `/photonics/palette.glsl`

### `VoxelData`
```glsl
struct VoxelData {

};
```
Stores block id & texture data for a voxel.


### `voxel_data_block_id`
```glsl
int voxel_data_block_id(VoxelData voxel_data);
```
Returns the block id of `voxel_data`.

### `voxel_data_albedo`
```glsl
vec4 voxel_data_albedo(VoxelData voxel_data);
```
Returns the albedo of `voxel_data`;

### `voxel_data_normal`
```glsl
vec4 voxel_data_normal(VoxelData voxel_data);
```
Returns the texture normal of `voxel_data`;

### `voxel_data_specular`
```glsl
vec4 voxel_data_specular(VoxelData voxel_data);
```
Returns the texture specular of `voxel_data`

## `/photonics/tracing.glsl`

### `RayResult`
```glsl
struct RayResult {

};
```
Hit result of a trace

### `missed_ray_result`
```glsl
RayResult missed_ray_result();
```
Returns missed ray result;

### `ray_result_is_hit`
```glsl
bool ray_result_is_hit(RayResult hit);
```
Returns `true` is `hit` is a hit.

### `ray_result_position`
```glsl
vec3 ray_result_position(RayResult hit);
```
Returns the position of `hit` in RT space. The result of this method when `hit` is a miss is unspecified.

### `ray_result_normal`
```glsl
vec3 ray_result_normal(RayResult hit);
```
Returns the result normal of `hit`. The result of this method when `hit` is a miss is unspecified.

### `ray_result_is_transparent`
```glsl
bool ray_result_is_transparent(RayResult hit);
```
Returns `true` if the voxel data of `hit` contains at least one transparent face. The result of this method when `hit` is a miss is unspecified.

This is useful the voxel data for a hit is fetched only at request.

### `ray_result_voxel_data`
```glsl
VoxelData ray_result_voxel_data(RayResult hit);
```
Returns the `VoxelData` of `hit`. 
This method will fetch the texture data at invocation, calling this method on the same RayResult multiple times should be avoided.
The result of this method when `hit` is a miss is unspecified.

### `ray_result_light_data`
```glsl
Light ray_result_light_data(RayResult hit);
```
Returns the `Light` of `hit`. If the ray hit no light, the returned light will be invalid.
The result of this method when `hit` is a miss is unspecified.

### `RayIterator`
```glsl
struct RayIterator {
    int iterations; // Defaults to 100
};
```

A stateful ray iterator struct.

### `ray_iter_set_position`
```glsl
void ray_iter_set_position(inout RayIterator ray, vec3 rt_position);
```

Sets position of `ray`.


### `ray_iter_offset_position`
```glsl
void ray_iter_set_position(inout RayIterator ray, vec3 offset);
```

Shifts the position of the iterator by `offset`. I.E `new_position = ray_position + offset`


### `ray_iter_set_direction`
```glsl
void ray_iter_set_direction(inout RayIterator ray, vec3 direction);
```
Sets the direction of `ray`. 
The position of the ray must have been initialized before this call. 

### `ray_iter_begin`
```glsl
void ray_iter_begin(inout RayIterator ray, vec3 rt_positon, vec3 direction);
```
Initializes `ray` with `rt_position` and `direction`. This is equivalent to the following code:

```glsl
RayIterator ray;
ray.iterations = <DEFAULT RAY ITERATIONS>;

ray_iter_set_position(ray, rt_position);
ray_iter_set_direction(ray, direction);
```

### `ray_iter_has_next`
```glsl
bool ray_iter_has_next(inout RayIterator ray);
```
Returns `true` if a subsequent call to `ray_iter_next` will return a hit RayResult. 
Rays that left the bounds of the world or ran out of iterations will cause this method to return `false`.

### `ray_iter_next`
```glsl
RayResult ray_iter_next(inout RayIterator ray);
```
Returns the next ray hit.

This will be the main description on how RayIterator behaves.

RayIterator is meant to mimic java's iterators, so you have 2 ways of using it.

```glsl
RayIterator ray;

RayResult result1 = ray_iter_next(ray);
RayResult result2 = ray_iter_next(ray);
RayResult result3 = ray_iter_next(ray);
```

and

```glsl
RayIterator ray;

while (ray_iter_has_next(ray)) {
    RayResult result = ray_iter_next(ray);
}
```

If a previous call to next returned a missed ray, subsequent calls to next will always return a miss until a call to `ray_iter_set_direction` or `ray_iter_begin`.
At any point you can make calls to ray_iter_set_position or ray_iter_set_direction to change the position/direction of the ray.

### `ray_iter_has_next_block`
```glsl
bool ray_iter_has_next_block(inout RayIterator ray, vec3 target);
```

Returns `true` if a subsequent call to `ray_iter_next_block` with `target` will return a hit RayResult.

### `ray_iter_next_block`
```glsl
RayResult ray_iter_next_block(inout RayIterator ray, vec3 target);
```
This behaves identically to `ray_iter_next` with the exception that if the ray hits `target` 
(and there is a non-empty block there, though this will probably change in a future build) the ray will terminate immediately.
The side effect of this means that the resulting `VoxelData` will be invalid and cannot be used. Every other attribute of RayResult can be accessed, however.

### `ray_iter_skip_block`
```glsl
void ray_iter_skip_block(inout RayIterator ray);
```
Skips the block the iterator is currently on. There are no restrictions on when this method can be used besides the ray must have been initialized.

### `ray_iter_skip_voxel`
```glsl
void ray_iter_skip_voxel(inout RayIterator ray);
```
Skips the voxel the iterator is currently on. There are no restrictions on when this method can be used besides the ray must have been initialized.

### `ray_iter_is_in_bounds`
```glsl
bool ray_iter_is_in_bounds(inout RayIterator);
```
Returns `true` if the ray is in the bounds of the world.

### `ray_iter_apply_transparency`
```glsl
void ray_iter_apply_transparency(inout vec4 accumulator, vec4 albedo);
```
Applies multiplicative blending to `accumulator` with `albedo`, where accumulator is initialized to `vec4(0.0f)`.

Can be used to tint the final albedo like so:

```glsl
vec3 result_color = mix(albedo, accumulator.rgb, accumulator.a);
```

## `/photonics/uniforms.glsl`

A common file containing all of photonics's uniforms behind an #if guard. 
This is used by every Photonics file that uses its uniforms. 
Useful to avoid duplicate uniform declarations.

## `/photonics/interface/world_interface.glsl`

```glsl
bool is_in_world();
```
`true` if the current fragment is in the world

```glsl
bool is_hand_at();
```
`true` if the current fragment is the hand. Yes this name is currently a typo

```glsl
vec3 load_player_position();
```
The current player pos of the current fragment. The offset required in load_fragment_data from 0.3 is not required

```glsl
void load_fragment_data(out vec3 geometry_normal, out vec3 texture_normal);
```
Loads the geometry/texture normal of the current fragment

```glsl
vec2 get_taa_jitter();
```
The TAA jitter for the current fragment, in NDC space.

## `/photonics/interface/lighting_interface.glsl`
```glsl
vec3 get_sun_direction();
```
The current direction to the sun or moon (depending on which one is the active light source)

```glsl
vec3 get_sun_color();
```
The color of light from the sun.

```glsl
vec3 get_sky_color();
```
The color of light from the sky.

```glsl
bool is_in_shadow_at(vec3 scene_pos, vec3 geo_normal);
```
`true` if `scene_pos` is in shadow. It is okay for this method to return false for culling/out of distance.

If you do not use shadow mapping at all define `NO_SHADOW_MAPPING` at the top of the file. 
This will force photonic's to use the old approach for checking trace to sun.

