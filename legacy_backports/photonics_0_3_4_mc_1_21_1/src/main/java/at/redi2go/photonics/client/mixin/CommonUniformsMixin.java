package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.RenderDispatcher;
import at.redi2go.photonics.client.rendering.MinecraftAccessor;
import at.redi2go.photonics.client.rendering.TemporalUniformState;
import at.redi2go.photonics.client.rendering.opengl.rendering.ShaderUtil;
import at.redi2go.photonics.client.rendering.world.WorldRegistry;
import java.util.function.Supplier;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommonUniforms.class, remap = false)
public class CommonUniformsMixin {
   @Unique
   private static TemporalUniformState photonics$getTemporalUniformState(Supplier<RenderDispatcher> renderDispatcher) {
      Vector3d cameraPosition = MinecraftAccessor.getCameraPosition();
      Matrix4f modelViewProjection = renderDispatcher.get()
         .getModelViewProjectionMatrix(new Vector3f((float)cameraPosition.x, (float)cameraPosition.y, (float)cameraPosition.z));
      TemporalUniformState.INSTANCE.update(SystemTimeUniforms.COUNTER.getAsInt(), modelViewProjection, cameraPosition);
      return TemporalUniformState.INSTANCE;
   }

   @Inject(method = "addNonDynamicUniforms", at = @At("TAIL"))
   private static void addIrisExclusiveUniforms(
      UniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier, CallbackInfo ci
   ) {
      if (Raytracer.shouldBeEnabled()) {
         Supplier<WorldRegistry> worldRegistry = () -> Raytracer.INSTANCE.getWorldRegistry();
         Supplier<RenderDispatcher> renderDispatcher = () -> Raytracer.INSTANCE.getRenderDispatcher();
         uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "world_camera_position", MinecraftAccessor::getCameraPosition);
         uniforms.uniformMatrix(
            UniformUpdateFrequency.PER_FRAME,
            "direction_transformation_matrix_in",
            () -> ShaderUtil.createScreenCameraMatrix(
               CapturedRenderingState.INSTANCE.getGbufferProjection(), CapturedRenderingState.INSTANCE.getGbufferModelView()
            )
         );
         uniforms.uniformMatrix(
            UniformUpdateFrequency.PER_FRAME,
            "modelview_projection",
            () -> {
               Vector3d pos = MinecraftAccessor.getCameraPosition();
               return renderDispatcher.get().getModelViewProjectionMatrix(new Vector3f((float)pos.x, (float)pos.y, (float)pos.z));
            }
         );
         uniforms.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "previous_modelview_projection", () -> {
            return photonics$getTemporalUniformState(renderDispatcher).getPreviousModelViewProjection();
         });
         uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "previous_world_camera_position", () -> {
            return photonics$getTemporalUniformState(renderDispatcher).getPreviousWorldCameraPosition();
         });
         uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "left_handed", () -> renderDispatcher.get().isLeftHanded());
         uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "main_hand_has_light", () -> renderDispatcher.get().hasMainHandLight());
         uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "ph_main_hand_light0", () -> renderDispatcher.get().getMainHandLight()[0]);
         uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "ph_main_hand_light1", () -> renderDispatcher.get().getMainHandLight()[1]);
         uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "ph_main_hand_light2", () -> renderDispatcher.get().getMainHandLight()[2]);
         uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "off_hand_has_light", () -> renderDispatcher.get().hasOffhandLight());
         uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "ph_off_hand_light0", () -> renderDispatcher.get().getOffHandLight()[0]);
         uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "ph_off_hand_light1", () -> renderDispatcher.get().getOffHandLight()[1]);
         uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "ph_off_hand_light2", () -> renderDispatcher.get().getOffHandLight()[2]);
         uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "light_reload", () -> worldRegistry.get().fetchLightReload());
         uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "phFirstBuildTime", () -> worldRegistry.get().getFirstBuildTime());
         uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "phLastBuildTime", () -> worldRegistry.get().getLastBuildTime());
      }
   }

   @Inject(method = "addDynamicUniforms", at = @At("TAIL"))
   private static void addIrisExclusiveUniforms(DynamicUniformHolder uniforms, FogMode fogMode, CallbackInfo ci) {
      if (Raytracer.shouldBeEnabled()) {
         Supplier<WorldRegistry> worldRegistry = () -> Raytracer.INSTANCE.getWorldRegistry();
         uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "world_offset", () -> worldRegistry.get().getWorldOffset());
         uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "world_min_voxel", () -> new Vector3d(worldRegistry.get().getWorldMinVoxel().toVector()));
         uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "world_max_voxel", () -> new Vector3d(worldRegistry.get().getWorldMaxVoxel().toVector()));
         uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "rt_camera_position", () -> worldRegistry.get().toRt(MinecraftAccessor.getCameraPosition()));
         uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "ph_light_count", () -> {
            Raytracer rt = Raytracer.INSTANCE;
            return rt == null ? 0 : rt.getWorldRegistry().getLightRegistry().lightCount();
         });
         Raytracer.INSTANCE.getMainRenderer().registerCustomUniforms(uniforms);
      }
   }
}
