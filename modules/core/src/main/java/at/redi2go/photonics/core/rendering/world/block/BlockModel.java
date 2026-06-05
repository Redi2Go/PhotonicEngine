package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.registry.light.WorldLight;
import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.Collection;
import java.util.List;

public interface BlockModel extends Disposable {
    List<Part> parts();

    interface Part extends Disposable {
        Vector3i offset();

        BlockEntry createEntry(
                int region,
                int skylight,
                @Nullable @WeakValue WorldLight light
        );
    }
}
