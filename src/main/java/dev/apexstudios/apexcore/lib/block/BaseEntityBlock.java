package dev.apexstudios.apexcore.lib.block;

import com.mojang.serialization.MapCodec;
import dev.apexstudios.apexcore.lib.block.entity.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseEntityBlock extends net.minecraft.world.level.block.BaseEntityBlock {
    protected BaseEntityBlock(Properties properties) {
        super(properties);
    }

    protected InteractionResult openMenu(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        var menuProvider = blockState.getMenuProvider(level, pos);

        if(menuProvider != null) {
            player.openMenu(menuProvider);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        var result = blockState;

        if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity)
            result = blockEntity.playerWillDestroy(level, result, player);

        return super.playerWillDestroy(level, pos, result, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        var interactionResult = openMenu(blockState, level, pos, player, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        return super.useWithoutItem(blockState, level, pos, player, result);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        var blockEntitySignal = level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity ? blockEntity.getAnalogOutputSignal(blockState, level) : 0;
        return blockEntitySignal + super.getAnalogOutputSignal(blockState, level, pos);
    }
}
