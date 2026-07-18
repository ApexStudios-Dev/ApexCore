package dev.apexstudios.apexcore.api.ghost;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record GhostBlockState(
        BlockState blockState,
        ModelData modelData,
        long seed,
        Vector3fc offset
) {
    public static final GhostBlockState EMPTY = new GhostBlockState(
            Blocks.AIR.defaultBlockState(),
            ModelData.EMPTY,
            0L,
            GhostPlacementExtractor.NO_OFFSET
    );

    public GhostBlockState {
        offset = new Vector3f(offset);
    }
}
