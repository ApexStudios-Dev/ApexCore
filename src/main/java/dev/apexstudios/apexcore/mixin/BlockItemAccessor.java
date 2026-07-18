package dev.apexstudios.apexcore.mixin;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockItem.class)
public interface BlockItemAccessor {
    @Invoker("getPlacementState")
    @Nullable
    BlockState ApexCore$getPlacementState(BlockPlaceContext placeContext);

    @Invoker("canPlace")
    boolean ApexCore$canPlace(BlockPlaceContext placeContext, BlockState blockState);
}
