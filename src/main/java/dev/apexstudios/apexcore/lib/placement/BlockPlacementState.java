package dev.apexstudios.apexcore.lib.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;

public interface BlockPlacementState {
    ItemStack stack();

    InteractionHand hand();

    BlockPos pos();

    Direction face();

    FeatureFlagSet enabledFeatures();
}
