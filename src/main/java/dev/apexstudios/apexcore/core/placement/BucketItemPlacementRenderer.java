package dev.apexstudios.apexcore.core.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementRenderer;
import dev.apexstudios.apexcore.mixin.BucketItemAccessor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

final class BucketItemPlacementRenderer implements BlockPlacementRenderer {
    @Override
    public boolean renderForHand(Level level, Player player, InteractionHand hand, BlockHitResult hitResult, Camera camera, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        var stack = player.getItemInHand(hand);

        if(!(stack.getItem() instanceof BucketItem item))
            return false;
        if(!(item.content instanceof FlowingFluid fluid))
            return false;
        if(!item.content.is(FLUID_WHITELIST))
            return false;

        var fluidResult = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        if(fluidResult.getType() != HitResult.Type.BLOCK)
            return false;

        var accessor = (BucketItemAccessor) item;
        var enabledFeatures = level.enabledFeatures();
        var canBePlaced = new AtomicBoolean(stack.isItemEnabled(enabledFeatures) && (!(item.content instanceof FeatureElement element) || element.isEnabled(level.enabledFeatures())));
        var pos = new AtomicReference<>(fluidResult.getBlockPos());
        var containedFluidStack = FluidUtil.getFluidContained(stack);

        if(!accessor.ApexCore$canBlockContainFluid(player, level, pos.get(), level.getBlockState(pos.get())))
            pos.set(pos.get().relative(fluidResult.getDirection()));

        if(containedFluidStack.isPresent() && fluid.getFluidType().isVaporizedOnPlacement(level, pos.get(), containedFluidStack.get()))
            canBePlaced.set(false);
        else if(level.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER))
            canBePlaced.set(false);
        else
            canBePlaced.set(accessor.ApexCore$canBlockContainFluid(player, level, pos.get(), level.getBlockState(pos.get())));

        var fluidState = item.content.defaultFluidState();
        var blockState = fluidState.createLegacyBlock();

        BlockPlacementRenderer.renderAt(camera, pose, () -> BlockPlacementRenderer.renderFluidState(level, pos.get(), blockState, fluidState, buffers, pose, canBePlaced.get()));
        return true;
    }
}
