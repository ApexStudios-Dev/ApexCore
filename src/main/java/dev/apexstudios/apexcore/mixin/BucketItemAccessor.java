package dev.apexstudios.apexcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BucketItem.class)
public interface BucketItemAccessor {
    @Invoker("canBlockContainFluid")
    boolean ApexCore$canBlockContainFluid(@Nullable Player player, Level level, BlockPos pos, BlockState blockState);
}
