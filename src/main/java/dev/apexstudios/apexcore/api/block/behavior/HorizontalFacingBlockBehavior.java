package dev.apexstudios.apexcore.api.block.behavior;

import dev.apexstudios.apexcore.api.util.ApexUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class HorizontalFacingBlockBehavior extends BlockBehavior {
    public static final BlockBehaviorType<HorizontalFacingBlockBehavior> TYPE = new BlockBehaviorType<>(HorizontalFacingBlockBehavior::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private HorizontalFacingBlockBehavior(BlockBehaviorRegistration registration) {
        super(registration);

        registration.property(FACING, Direction.NORTH);
    }

    public Direction get(BlockState blockState) {
        return blockState.getValueOrElse(FACING, Direction.NORTH);
    }

    public BlockState set(BlockState blockState, Direction facing) {
        return facing.getAxis().isHorizontal() ? blockState.trySetValue(FACING, facing) : blockState;
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

    @Override
    protected BlockState copyProperties(BlockState blockState, BlockState neighborBlockState) {
        return set(blockState, get(blockState));
    }
}
