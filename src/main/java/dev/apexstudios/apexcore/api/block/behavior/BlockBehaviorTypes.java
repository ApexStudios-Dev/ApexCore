package dev.apexstudios.apexcore.api.block.behavior;

public interface BlockBehaviorTypes {
    BlockBehaviorType<WaterLoggedBlockBehavior> WATERLOGGED = WaterLoggedBlockBehavior.TYPE;
    BlockBehaviorType<HorizontalFacingBlockBehavior> HORIZONTAL_FACING = HorizontalFacingBlockBehavior.TYPE;
    BlockBehaviorType<DyeableBlockBehavior> DYEABLE = DyeableBlockBehavior.TYPE;
}
