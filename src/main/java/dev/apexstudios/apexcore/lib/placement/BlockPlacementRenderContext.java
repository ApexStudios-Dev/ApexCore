package dev.apexstudios.apexcore.lib.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record BlockPlacementRenderContext(
        Level level,
        BlockPos pos,
        Direction face,
        BlockHitResult hitResult,

        Player player,
        HumanoidArm arm,
        ItemResource item
) {
    public BlockPlacementRenderContext {
        pos = pos.immutable();
    }

    public BlockPlacementRenderContext(Level level, BlockHitResult hitResult, Player player, HumanoidArm arm) {
        this(level, hitResult.getBlockPos(), hitResult.getDirection(), hitResult, player, arm, ItemResource.of(player.getItemHeldByArm(arm)));
    }

    public InteractionHand hand() {
        return arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public BlockPlacementRenderContext at(Level level, BlockPos pos, Direction face) {
        return new BlockPlacementRenderContext(level, pos, face, hitResult, player, arm, item);
    }
}
