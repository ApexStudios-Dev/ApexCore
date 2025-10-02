package dev.apexstudios.apexcore.core.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementState;
import dev.apexstudios.apexcore.lib.placement.ExtractBlockPlacementState;
import dev.apexstudios.apexcore.lib.placement.SubmitBlockPlacementState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public record BlockPlacementRender<T>(ContextKey<T> key, ExtractBlockPlacementState<T> extractor, SubmitBlockPlacementState<T> submit) {
    public boolean extract(Level level, LevelRenderState levelState, Player player, BlockHitResult hitResult, BlockPlacementState placementState) {
        var state = extractor.extract(level, levelState, player, hitResult, placementState);

        if(state == null)
            return false;

        levelState.setRenderData(key, state);
        return true;
    }

    public void submit(LevelRenderState levelState, BlockPlacementState placementState, PoseStack pose, SubmitNodeCollector collector) {
        var state = levelState.getRenderDataOrThrow(key); // should never be null
        submit.submit(state, levelState, placementState, pose, collector);
    }
}
