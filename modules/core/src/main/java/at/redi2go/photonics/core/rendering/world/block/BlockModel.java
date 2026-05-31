package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.api.Disposable;
import org.joml.Vector3i;

import java.util.Collection;
import java.util.List;

public interface BlockModel {
    List<Part> parts();

    interface Part extends Disposable {
        Vector3i offset();

        BlockEntry createEntry(int region);
    }
}
