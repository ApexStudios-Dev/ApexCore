package dev.apexstudios.apexcore.lib.placement;

import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ExtractBlockPlacementState<T> {
    @Nullable
    T extract(Level level, LevelRenderState levelState, Player player, BlockHitResult hitResult, BlockPlacementState placementState);
}
