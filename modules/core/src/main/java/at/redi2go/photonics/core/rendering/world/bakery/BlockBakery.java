package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.rendering.world.bakery.impl.BlockBakeryImpl;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public interface BlockBakery {
    @Nullable <T extends BlockMeshState> MeshResult meshBlock(
            BlockMesher<T> mesher,
            T meshState,
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    );

    interface MeshResult extends Disposable {
        long vertexHash();

        TintBuilder.Result tintData();

        void bake(VoxelConsumer voxelConsumer) throws InterruptedException;
    }

    static BlockBakery newBakery(AtlasDownloader atlasDownloader) {
        return new BlockBakeryImpl(atlasDownloader);
    }
}
