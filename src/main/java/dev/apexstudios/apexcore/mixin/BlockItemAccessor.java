package dev.apexstudios.apexcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockItem.class)
public interface BlockItemAccessor {
    @Nullable
    @Invoker("getPlacementState")
    BlockState ApexCore$getPlacementState(BlockPlaceContext context);

    @Invoker("placeBlock")
    boolean ApexCore$placeBlock(BlockPlaceContext context, BlockState blockState);

    @Invoker("updateBlockStateFromTag")
    BlockState ApexCore$updateBlockStateFromTag(BlockPos pos, Level level, ItemStack stack, BlockState blockState);

    @Invoker("updateCustomBlockEntityTag")
    boolean ApexCore$updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState blockState);
}
