package dev.apexstudios.apexcore.api.block.behavior;

import dev.apexstudios.apexcore.api.util.ApexUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class HorizontalFacingBlockBehavior extends SimplePropertyBlockBehavior<Direction, EnumProperty<Direction>> {
    public static final BlockBehaviorType<HorizontalFacingBlockBehavior> TYPE = new BlockBehaviorType<>(HorizontalFacingBlockBehavior::new);

    private HorizontalFacingBlockBehavior(BlockBehaviorRegistration registration) {
        super(registration, BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
    }

    @Override
    public BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext context) {
        var facing = context.getHorizontalDirection().getOpposite();

        if(ApexUtil.hasModifierKeyPressed(context.getPlayer())) {
            facing = facing.getOpposite();
        }

        return set(blockState, facing);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return set(blockState, rotation.rotate(get(blockState)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(get(blockState)));
    }
}
