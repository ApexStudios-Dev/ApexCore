package dev.apexstudios.apexcore.api.block.behavior;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface BlockBehaviorTypes {
    BlockBehaviorType<WaterLoggedBlockBehavior> WATERLOGGED = WaterLoggedBlockBehavior.TYPE;
    BlockBehaviorType<HorizontalFacingBlockBehavior> HORIZONTAL_FACING = HorizontalFacingBlockBehavior.TYPE;
    BlockBehaviorType<DyeableBlockBehavior> DYEABLE = DyeableBlockBehavior.TYPE;

    BlockBehaviorType<SimplePropertyBlockBehavior<Boolean, BooleanProperty>> OCCUPIED = SimplePropertyBlockBehavior.createBoolean(BlockStateProperties.OCCUPIED);
}
