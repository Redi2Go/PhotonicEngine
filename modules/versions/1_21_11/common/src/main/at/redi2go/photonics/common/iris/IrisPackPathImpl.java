package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.engine.iris.IrisPackPath;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(AbsolutePackPath.class)
public abstract class IrisPackPathImpl implements IrisPackPath {
    @Shadow
    public abstract Optional<AbsolutePackPath> parent();

    @Shadow
    public abstract AbsolutePackPath resolve(String path);

    @Shadow
    public abstract Path resolved(Path root);

    @Shadow @Final private String path;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<IrisPackPath> ph$parent() {
        return (Optional) parent();
    }

    @Override
    public IrisPackPath ph$resolve(String path) {
        return (IrisPackPath) resolve(path);
    }

    @Override
    public Path ph$resolved(Path root) {
        return resolved(root);
    }

    @Override
    public boolean ph$startsWith(IrisPackPath path) {
        return this.path.startsWith(((AbsolutePackPath) path).getPathString());
    }

    @Override
    public String ph$pathString() {
        return path;
    }

    @Mixin(IrisPackPath.class)
    public interface StaticMethods {
        @Overwrite
        static IrisPackPath fromAbsolutePath(String absolutePath) {
            return (IrisPackPath) AbsolutePackPath.fromAbsolutePath(absolutePath);
        }
    }
}
