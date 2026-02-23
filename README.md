# DOCUMENTATION
This section documents all properties, macros, uniforms, samplers, and provided functions.

# Properties
Photonics uses [shaders.properties](https://shaders.properties/current/reference/shadersproperties/overview/) to configure its internal settings. 
Most of these support the use of [preprocessor directives](https://registry.khronos.org/OpenGL/specs/gl/GLSLangSpec.4.60.html#preprocessor), those which do not will be marked as such.

```
photonics.enabled = <true | false>
```
Enable/disable the use of Photonics. Only works for regular shaders.
Defaults to `false`.

<br></br>
```
photonics.useDeferredPass = <true | false>
```
Enable/disable the use of Photonics's deferred lighting pass. 
Defaults to `true`

<br></br>
```
photoncs.supported = <true | false>
```
Used by patches to indicate whether Photonics is supported. 
Does not support the use of the preprocessor. 
DO NOT USE IN REGULAR SHADERS

<br></br>
```
photonics.renderScale = <float>
```
Sets the render resolution photonics renders GI/Blocklight at, as a factor of the screen resolution (E.g. 0.5 renders at half resolution). 
Defaults to `1`.

<br></br>
```
photonics.maxLights = <unsigned int>
```
Sets the maximum number of lights that can be loaded by Photonics. Must be a positive integer greater than 0. 
Defaults to `1000`.

<br></br>
```
photonics.maxSamples = <unsigned int>
```
Sets the maximum number of lights that can be sampled per fragment. Must be a positive integer greater than 0.
Defaults to `20`.

This setting is analogous to 'Max Light Samples' in [rethinking voxels](https://modrinth.com/shader/rethinking-voxels).

<br></br>
```
photonics.alphaMode = <none | block | voxel>
```
Sets the way photonics handles transparent voxels during tracing.
   * `none`: Treats transparent voxels as opaque. (no performance impact)
   * `block`: Applies tint based on the first transparent voxel, then skips tracing to the next block. (small performance impact)
   * `voxel`: Applies tint based on every encountered transparent voxel. (large performance impact)
 
Supports any case. (E.g. `NONE` and `none` are both supported).
Defaults to `none`.

<br></br>
```
photonics.enableGi = <true | false>
```
Enable/disable GI tracing during the lighting pass.
Default to `true`

<br></br>
```
photonics.enableBlockLight = <true | false>
```
Enable/disable block light tracing during the lighting pass.
Defaults to `true`.

<br></br>
```
photonics.enableHandheldLight = <true | false>
```
Enable/disable handheld tracing during the lighting pass.
Defaults to `true`.


<br></br>
<br></br>
# Macros

### `PHOTONICS`
Defined in shaders and properties files at all times when Photonics is installed.

### `PHOTONICS_VERSION`
The current Photonics version, encoded in a 122 format (1 major, 2 minor, 2 incremental), with leading zeros removed.
E.g. 0.2.10 would be 210.

Defined in shaders and properties files at all times.

See [iris version](https://shaders.properties/current/reference/macros/iris_version/).

### `PHOTONICS_ENABLED`
Defined by patches when Photonics is enabled.
Does nothing in regular shaders.

### `PH_RENDER_SCALE`
The current value for `photonics.renderScale`. Defined in shaders at all times.

### `PH_MAX_LIGHTS`
The current value for `photonics.maxLights`. Defined in shaders at all times.

### `PH_MAX_LIGHTS`
The current value for `photonics.maxSamples`. Defined in shaders at all times.

### `PH_ENABLE_GI`
Defined in shaders when `photonics.enableGi` is `true`

### `PH_ENABLE_BLOCKLIGHT`
Defined in shaders when `photonics.enableBlockLight` is `true`

### `PH_ENABLE_HANDHELD_LIGHT`
Defined in shaders when `photonics.enableHandheldLight` is `true`

### `OVERWORLD`
Defined in shaders under the `/photonics` directory when in [the overworld](https://minecraft.wiki/w/Overworld).

### `NETHER`
Defined in shaders under the `/photonics` directory when in [the nether](https://minecraft.wiki/w/The_Nether).

### `END`
Defined in shaders under the `/photonics` directory when in [the end](https://minecraft.wiki/w/The_End).

### `#PH_USE_CUSTOM_ALPHA`
Used to enable the use of a custom alpha func in `trace_ray`. By default `trace_ray` uses absorption.

### `#PH_ALPHA_FUNC`
Sets the alpha func used by `trace_ray`. 

An example for regular glass rendering would be

```glsl
#PH_USE_CUSTOM_ALPHA
#PH_ALPHA_FUNC(color) apply_tint_impl(color)

vec3 apply_tint_impl(vec4 color) {
  return color.xyz * (1f - color.a);
}
```

<br></br>
# Uniforms

```glsl
uniform vec3 world_offset;
```
The origin of the accel structure, in blocks.

<br></br>
```glsl
uniform vec3 world_camera_position;
```
The camera position in world space.

<br></br>
```glsl
uniform vec3 rt_camera_position;
```
The camera position in the rt coordinate space. (E.g `rt_camera_position = world_camera_position - world_offset`)

<br></br>
```glsl
uniform vec3 handheld_color;
```
The color of the current held items. Will be `vec3(0f)` when no light is being held.

<br></br>
```glsl
uniform bool light_reload;
```
`true` for a single frame after loaded lights have changed.

### Samplers

```glsl
uniform sampler2D radiosity_direct;
```
Written to by Photonics during the lighting pass. Stores the light contribution by blocks.
Can be sampled using
```glsl
vec3 ph_direct = texture2D(radiosity_direct, gl_FragCoord).rgb;
```

<br></br>
```glsl
uniform sampler2D radiosity_direct_soft;
```
Written to by Photonics during the lighting pass. Stores the accumulated soft light contribution by blocks.
Can be sampled using
```glsl
vec4 ph_direct_soft = texture2D(radiosity_direct_soft, gl_FragCoord);
vec3 color = ph_direct_soft.rgb / max(ph_direct_soft.a, 1.0f);
```

Where the alpha stores the number of samples.

<br></br>
```glsl
uniform sampler2D radiosity_handheld;
```
Written to by Photonics during the lighting pass. Stores the light contribution by handheld lighting.
Can be sampled using
```glsl
vec3 ph_handheld = texture2D(radiosity_handheld, gl_FragCoord).rgb;
```

### Provided functions/structs

## /photonics/photonics.glsl
This file `#includes`: 
    `photonics/ph_core.glsl`
    `photonics/ph_raytracing.glsl`

If you plan to use this outside of the `/photonics/` directory, 
you should add an empty `shaders/photonics/photonics.glsl` in your shader, 
otherwise your shaderpack will not load without photonics.

<br></br>
```glsl
struct RayJob {
    vec3 origin; // The origin of the ray in rt space. (E.g real_origin - world_offset)
    vec3 direction; // The direction of the ray.

    vec3 result_position; // The position of the hit voxel in rt space.
    vec3 result_normal; // The surface normal of the voxel that was hit
    vec3 result_color; // The color of the voxel that was hit
    bool result_hit; // true if the ray hit a voxel
};
```
Represents a ray, used by trace_ray.

<br></br>
```glsl
struct Light {
    vec3 position; // Position of the light rt space.
    vec3 color; // The color of the light. The result of of (color * intensity) in ph_lights.json.
    vec2 attenuation; 
    float falloff; // How fast the light fallsoff over distances. Used like (distance_squared * falloff).
};
```
Represents a block light. Instances of this struct can be obtained by `load_light` in `/photonics/ph_core.glsl`. 

## /photonics/ph_core.glsl
Should not be included directly, instead include only `photonics/photonics.glsl`

```glsl
Light load_light(int index);
```
Returns the light at (index), where index is an integer from 0 to PH_MAX_LIGHTS. 
Usable in every pass/program. This method will return garbage when `photonics.enableBlockLight` is `false.

## /photonics/ph_raytracing.glsl
Should not be included directly, instead include only `photonics/photonics.glsl`

```glsl
ivec3 ray_constraint;
```
When set limits raytracing to a single block in rt space at `ray_constraint`. To remove the constraint set `ray_constraint` to `ivec3(-9999)`.

<br></br>
```glsl
vec3 result_tint_color;
```
The cumulative tint color applied to lighting after a call to trace_ray.
This is always `vec3(1f)` when `photonics.alphaMode` is set to `none`.  Usable in every pass/program.

This defaults to absorption, but can be changed with [#PH_USE_CUSTOM_ALPHA](#ph_use_custom_alpha) and [#PH_ALPHA_FUNC](#ph_alpha_func)

<br></br>
```glsl
int result_block_id;
```
The [block id](https://shaders.properties/current/reference/miscellaneous/block_properties/) of the block that was hit.

<br></br>
```glsl
int get_result_sky_light(vec3 normal);
```
The skylight level of the hit block, where normal is one of the faces of the block. Ranges from 0 to 15.

<br></br>
```glsl
void trace_ray(
    inout RayJob job, // The ray to trace
    bool transparency // Whether or not to pass through transparent voxels. This does nothing when `photonics.alphaMode` is set to `none`.
);
```
Raytracing primitive. See documentation for RayJob for more details. Usable in every pass/program.

<br></br>
```glsl
void trace_ray(
    inout RayJob job // The ray to trace
);
```
An overload of `trace_ray(RayJob, bool)` where transparency is set to `false`. Usable in every pass/program.

# SPECIAL FILES
These files are used by photonics to interface with a shader, in a way this is the true 'api' of photonics. 
These files are expected to be created by a shader (or patched by a [patch](#patching)) with the following implementations.

Photonics provides default versions for patches to replace, however creating them in your shader will replace the versions provided by Photonics. 
(Note: this applies to most files in photonics, the only exception to this is `/photonics/photonics.glsl`).

For more information see [How to adapt your Shaderpack](#how-to-adapt-your-shaderpack).


## /photonics/write_indirect.glsl

```glsl
void write_indirect(
    vec3 color // The gi contribution for the current fragment
);
```
This function is used by Photonics to store the GI contribution for the current fragment.
<br></br>
These writes are thread safe, meaning for each fragment this function is only called *once*
<br></br>
For example, you could store (color) in a [custom image](https://shaders.properties/current/reference/buffers/custom_images/),
and sample like you would with `radiosity_direct`.

## /photonics/shader_interface.glsl

```glsl
vec3 load_world_position();
```
This function is used by Photonics to obtain the current position of a fragment in world space.

<br></br>
```glsl
void load_fragment_variables(
    out vec3 albedo, // The albedo of the current fragment
    out vec3 world_pos, // The world pos of the current fragment, after accounting for world_normal
    out vec3 world_normal, // The world normal for the current fragment
    out vec3 world_normal_mapped // The normal from the normal map of the current fragment
);
```
This function is used by Photonics to obtain general information about the current fragment. 
The implementation is expected to set `albedo`, `world_pos`, `world_normal`, and `world_normal_mapped`.

For more information see [How to adapt your Shaderpack](#how-to-adapt-your-shaderpack).

<br></br>
```glsl
vec3 sun_direction;
```
The current sun direction.

<br></br>
```glsl
vec3 indirect_light_color;
```
The indirect light color used by Gi.

<br></br>
```glsl
vec2 get_taa_jitter();
```
The taa jitter for the current fragment.

<br></br>
```glsl
bool is_in_world();
```
Returns `true` if the current fragment is in the world.

# ph_lights.json

Allows shaders to customize the color of block lights. Created under `/shaders/ph_lights.json`, it supports the preprocessor.

A version of `ph_lights.json` that contains the default light values is included in this repository.


The default priority for lights is: user config → mods → shader ph_lights.json.

## Variables

Variables allow you to reference previously defined types.

They are used like so `"key": "*<variable_name>"`.
The source they pull from will be listed under the type.

Variables may also reference other variables.

### Color
An rgb color.

Pulls variables from `defines.colors` in `ph_lights.json`

A color uses a string and supports 2 formats:

1. Hex color (E.g. `#FFFFFF`); must be a length of 7.
2. Rgb color, where each component is a float from 0 to 1 (E.g. `rgb(1.0, 1.0, 1.0)`)

### Intensity
Sets how bright the light is, where 1 is the normal brightness.

Pulls variables from `defines.intensities` in `ph_lights.json`
Uses a number

### Radius
The radius of a light. This is not in any specific unit, play around and see what works for you.

Pulls variables from `defines.radii` in `ph_lights.json`
Uses a number

### Falloff
How fast the light falls off over distances, where 1 is the default falloff (E.g. a falloff of 0.5 will falloff at half the rate of 1).

Pulls variables from `defines.falloffs` in `ph_lights.json`
Uses a number

### Block states
Represents block state(s). Each block state uses the same syntax as [/execute if block](https://minecraft.wiki/w/Commands/execute#ifunlessblock).

Block states are unique in that (in shaders) it pulls variables from `blocks.properties` (E.g. `*1000` will use the block states at id 1000).
It also supports 2 types:

1. A basic string (E.g `minecraft:#stone` is all stone blocks, `minecraft:furnace[lit=true]` is a lit furnace, and `*<id>` references blocks from `blocks.properties`).
2. A json object `{ "value": "<basic string>", "priority": <priority> }`, or `{ "value": "*<id>", "priority": <priority> }`

Blocks defined by shaders have their priority increased by 1000.

### Light Groups

A light group is a tree structure with the following fields:

```json
{
  "intensity": <intensity>,
  "radius": <radius>,
  "falloff": <falloff>,
  "color": <color>,
  "is_traced": <true | false>,
  "blocks": [
      "<block>",
      { "value": "<block>", "priority": "200" },
      ...
  ],
  "overrides": {
    "<group name>": <light group>
     ...
  }
}
```

You are allowed to omit some of these fields (such as intensity, radius, falloff, ect.).
If a field is omitted it inherits its value from its parents.

However every block **must** be able to resolve all the required fields.

## Structure

```json
{
  "defines": {
    "colors": {
      "example_color": "#ffffff",
      "example_color2": "rgb(1.0, 1.0, 1.0)"
    },
    "intensities": {
      "default": 1
    },
    "radii": {
      "default": 1
    },
    "falloffs": {
      "default": 1
    }
  },
  "lights": {
      "<group name>": <light group>
      ...
    }
  }
}
```

# Patching

This section will exclusively cover the patching system of Photonics and how it works. 
To actually add Photonics support to a shader you should follow [How to adapt your Shaderpack](#how-to-adapt-your-shaderpack).

# Loading custom patches

Patches are loaded from 3 places:
1. `.minecraft/shader-patches`
2. `src/resources/photonics/shaders/patches` (for dev environments)
3. The photonics jar

This will mostly focus on the 1st option as it's the most accessible.

Patches in `.minecraft/shader-patches` can either be
1. A folder
2. A zipped (.zip) archive.

## How patching works

### Runtime Patching Flow

1. When a shader pack is loaded, Photonics checks if a compatible patch exists
2. Each patch file either:
    - **Replaces** specific code sections in existing shader files
    - **Creates** new shader files from scratch
3. If `debug` in `patch.json` is set to `true`, patched files are written to `.minecraft/debug/`.

### Patch Matching

A patch is applied when:
- The shader pack name matches one of the names in `shaderPackNames` in `patch.json`
- The Photonics mod version is supported (a warning will be logged if version mismatch)

### Patching settings

When patching settings you likely want some settings to always be shown,
even when Photonics is disabled by the user (E.g. A 'Photonics Enabled' setting).

To accomplish this patches are given an `alwaysPatched` setting in `patch.json`.
Patch files listed in `alwaysPatched` will, as the name suggests, 
always be patched regardless of whether the user has Photonics enabled.

The path should be the full path of the file *in the shader*

Lets use this shader as an example:

```
example_shader/
└── shaders
    ├── lib
    │   └── settings.glsl    # We want to always patch this
    ├── shaders.properties
    └── ...                  # Other shader files
```

To always patch `settings.glsl` you should add `/libs/settings.glsl` to `alwaysPatched`

## Patch structure

### Directory Layout

Photonics places no restrictions on how you structure your patch. You are free to use whatever folders you want.

The only requirements are `patch.json` be in the root of the patch as seen below:

```
shader_patches/
└── example_patch/
    ├── patch.json          # Metadata and configuration
    └── ...                 # Other patch files
```

### patch.json format

The `patch.json` should contain the follow fields:

```json
{
  "formatVersion": 1,
  "shaderPackNames": ["<shaderpack name>"],
  "supportedVersions": ["<supported photonic version>"],
  "debug": true,
  "alwaysPatched": [
    "/lib/settings.glsl",
    "/shaders.properties",
    "/lang/en_US.lang"
  ]
}
```

**Fields:**
- `formatVersion` - Patch format version (currently 1)
- `shaderPackNames` - Array of shader pack names this patch applies to
- `supportedVersions` - Array of compatible Photonics mod versions
- `debug` - Whether to output patched files to debug folder
- `alwaysPatched` - Files to patch even when Photonics is disabled

### Patch File Format

Patch files use a custom syntax with directives starting with `#`:

#### Comments

```c
// This is a comment and will be ignored
```

#### File Directive

Specifies the target file(s) in the shader pack:

```c
#file "/program/gbuffers_terrain.glsl"
```

You can specify multiple files to patch them all with the same changes:

```c
#file "/program/gbuffers_terrain.glsl" "/program/gbuffers_water.glsl"
```

All file paths must start with `/` and are automatically prefixed with `/shaders`.

#### Template Directive

Specifies the source file(s) to use as a base for patching:

```c
#template "/program/gbuffers_terrain.glsl"
```

If omitted, the template defaults to the file specified in `#file`. You can use a single template for multiple files:

```c
#file "/program/gbuffers_voxels.glsl"
#template "/program/gbuffers_terrain.glsl"
```

Or specify one template per file:

```c
#file "/program/file1.glsl" "/program/file2.glsl"
#template "/program/template1.glsl" "/program/template2.glsl"
```

#### Replace Directive

Replaces a specific string in the template file with new content:

```c
#replace "float skyOcclusion = 0.0;"
float skyOcclusion = 0.0;
vec3 oldAlbedo = albedo.xyz;
#endreplace
```

The string to search for is specified in quotes after `#replace`. 
Everything between `#replace` and `#endreplace` becomes the replacement text.

New lines (`\n`) are not supported.

**Example with multiline search:**

```c
#replace "#include "/lib/settings.glsl""
#include "/lib/settings.glsl"

#undef PARALLAX
#undef DYNAMIC_HANDLIGHT
#undef REFLECTION_RAIN
#undef EMISSIVE_RECOLOR
#undef NORMAL_PLANTS
#undef SELF_SHADOW
#endreplace
```

#### Create Directive

Creates a new file from scratch:

```c
#file "/photonics/ph_indirect.glsl"
#create
layout(location = 0) out vec4 fragColor;
void write_indirect(vec3 color) {
    /* RENDERTARGETS:12 */
    fragColor = vec4(color, 1.0f);
}
```

Everything after `#create` becomes the file content. This is useful for adding entirely new shader files.


# How to adapt your Shaderpack

This section **will not** cover adding photonics settings to your shaderpack, and expects you have read the [documentation](#DOCUMENTATION).
To understand what options Photonics gives you access to see [properties](#properties).



There are two ways to add Photonics to your shaderpack
1) Use the provided photonics pass (in deferred) that accepts gbuffer data and populates a lighting buffer; this is the easy route where photonics does everything for you
2) Use the tracing API directly, works in gbuffers, you are responsible for usage and optimization.

This section will only cover option 1.
An example implementation of Photonics for Photon can be found at https://github.com/Essentuan/photon/tree/photonics-example


### For patching
If you are making a [patch](#patching) for an existing shader, you need to do 2 things.

1. Add `photonics.supported=true` to [shaders.properties](https://shaders.properties/current/reference/shadersproperties/overview/).
2. Create a `#PHOTONICS_ENABLED` define in your shaders settings file. `#PHOTONICS_ENABLED` being defined is what enables Photonics for patches!

# Adding lighting
To add raytraced lighting to your shaderpack you need to create 2 files:

## /photonics/shader_interface.glsl
Create a file under `/shaders/photonics/` called `shader_interface.glsl`

This is the main file Photonics uses to obtain information from your shader. 
It's used by Photonics during its lighting pass in a fragment shader.

You need to implement the following functions:

<br></br>
```glsl
vec3 load_world_position();
```
Should return the world position for the current fragment. 

<br></br>
```glsl
void load_fragment_variables(
    out vec3 albedo,
    out vec3 world_pos,
    out vec3 world_normal,
    out vec3 world_normal_mapped
);
```
This function is responsible for loading information about the current fragment into its arguments.

```glsl
out vec3 albedo;
```

This argument is pretty simple, it's just the albedo for the current fragment.

```glsl
out vec3 world_normal;
```

The normal for the current fragment in world space.

```glsl
out vec3 world_normal_mapped;
```

The normal from the normal map of the current fragment in world space.

```glsl
out vec3 world_pos;
```

This argument is the world position for the current fragment, you first however need to nudge the position outside of the block.
To do this you can subtract `0.01f * world_normal` from `load_world_position()`


<br></br>
```glsl
vec3 sun_direction;
```
The direction of the sun in world space.

<br></br>
```glsl
vec3 indirect_light_color;
```
The indirect light color used by Gi, probably should be your sky color.

<br></br>
```glsl
vec2 get_taa_jitter();
```
Should return the taa jitter for the current fragment.

```glsl
bool is_in_world();
```
Should return `true` if the current fragment is in the world.

In most cases this can default to:
```glsl
bool is_in_world() {
    return texelFetch(depthtex0, ivec2(gl_FragCoord.xy), 0).x <= 0.99999f;
}
```


### /photonics/write_indirect.glsl
Create a file under `/shaders/photonics/` called `write_indirect.glsl`

Photonics uses this file during its lighting pass in a fragment shader.

You need to implement the following functions:

```glsl
void write_indirect(vec3 color);
```

This function writes the GI contribution for a fragment. 
Photonics doesn't store GI in an accessible way for you, you instead must store and sample it yourself.

## Sampling

To sample light contributions see [sampers](#samplers). 

# Programs

Photonics supports a special kind of rendering (referred to here as voxelized blocks) for blocks that utilizes its Volume Rendering techniques that are used for all other graphical effects in Photonics.
Under the hood, voxelized blocks have the same mesh as a normal solid block (e.g. Stone Block). 
The only difference, is that they use 2 custom programs: gbuffers_voxels and shadow_voxels.

Voxelized blocks *do not support* any PBR features or transparency. 

## gbuffers_voxels

Responsible for rendering voxelized blocks for terrain.

A mock implementation for `gbuffers_voxels.fsh` for sampling albedo & normals.
This implementation requires `block_normal` from the vertex shader, which is equal to `gl_Normal`

```glsl
// Placeholder for obtaining the screen pos
vec3 screen_pos = get_screen_pos();

// These functions are placeholders for however you do these conversions
vec3 view_pos = screen_to_view_space(screen_pos); // If you use TAA this should account for jitter
vec3 scene_pos = view_to_scene_space(view_pos);

vec3 world_pos = scene_pos + cameraPosition;

RayJob ray = RayJob(
    // Translates from world space to rt space
    // The offset along the normal is very important to ensure that the ray doesn't start outside the block
    world_pos - world_offset - 0.001f * block_normal, // Ray origin

    // View direction of the current pixel is calculated by subtracting the camera position from the world position
    normalize(scene_pos - gbufferModelViewInverse[3].xyz), // Ray direction
    
    // Initialize results to default
    vec3(0), vec3(0), vec3(0), false
);

// stop raytracing once the ray leaves the block
ray_constraint = ivec3(ray.origin);
trace_ray(ray);

// Ray didn't hit any part of the 3d block (e.g. corners of the Crafting Table) 
if (!ray.result_hit) discard;
if (ray.result_normal == vec3(0.0)) ray.result_normal = block_normal;

scene_pos = ray.result_position + world_offset - cameraPosition;

// These functions are placeholders for however you do these conversions
view_pos = scene_to_view_space(scene_pos);
screen_pos = view_to_screen_space(view_pos);

// Also update the depth!
gl_FragDepth = screen_pos.z;

vec3 world_normal = ray.result_normal; // The normal in world space 
vec4 albedo = vec4(ray.result_color, 1f);
```

## shadow_voxels

Responsible for rendering voxelized blocks in shadows.

A mock implementation for `shadow_voxels.fsh` for detecting hits.
This implementation requires:

1. `block_normal` from the vertex shader, which is equal to `gl_Normal`
2. `world_pos` from the vertex shader, which is the position of the vertex in world space.
```glsl
RayJob ray = RayJob(
    // Translates from world space to rt space
    // The offset along the normal is very important to ensure that the ray doesn't start outside the block
    world_pos - world_offset - 0.01f * block_normal, // Ray origin
        
    mat3(shadowModelViewInverse) * vec3(0f, 0f, -1f), // Ray direction

    // Initialize results to default
    vec3(0f), vec3(0f), vec3(0f), false
);

ray_constraint = ivec3(ray.origin);
trace_ray(ray);

// Ray didn't hit any part of the 3d block (e.g. corners of the Crafting Table) 
if (!ray.result_hit) discard;

// In shadow!

// If you need the color you can use this, but it will always be opaque.
vec4 voxel_color = vec4(ray.result_color, 1f);
```
