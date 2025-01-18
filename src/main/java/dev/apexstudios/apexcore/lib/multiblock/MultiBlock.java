package dev.apexstudios.apexcore.lib.multiblock;

import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface MultiBlock {
    int ORIGIN_INDEX = 0;

    MultiBlockType getMultiBlockType();

    static BlockPos getBlockEntityPos(BlockPos pos, BlockState blockState) {
        var component = BlockComponentHelper.getComponent(blockState, BlockComponentTypes.MULTI_BLOCK);

        if(component != null) {
            var multiBlockType = component.getMultiBlockType();

            if(multiBlockType.indexOf(blockState) != ORIGIN_INDEX)
                return multiBlockType.getOrigin(pos, blockState);
        }

        return pos;
    }

    static BlockPos getBlockEntityPos(BlockGetter level, BlockPos pos) {
        return getBlockEntityPos(pos, level.getBlockState(pos));
    }

    static VoxelShape fixVoxelShape(VoxelShape shape, MultiBlockType multiBlockType, BlockState blockState, BlockPos pos) {
        var origin = multiBlockType.getOrigin(pos, blockState);
        var offset = pos.subtract(origin);
        return shape.move(-offset.getX(), -offset.getY(), -offset.getZ());
    }
}
