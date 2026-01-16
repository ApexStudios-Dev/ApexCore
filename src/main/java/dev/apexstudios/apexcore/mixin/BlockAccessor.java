package dev.apexstudios.apexcore.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor("stateDefinition")
    @Mutable
    void ApexCore$setStateDefinition(StateDefinition<Block, BlockState> stateDefinition);

    @Invoker("createBlockStateDefinition")
    void ApexCore$createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder);

    @Invoker("registerDefaultState")
    void ApexCore$registerDefaultState(BlockState defaultBlockState);
}
