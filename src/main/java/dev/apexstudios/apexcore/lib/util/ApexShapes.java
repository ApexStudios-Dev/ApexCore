package dev.apexstudios.apexcore.lib.util;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface ApexShapes {
    static VoxelShape joinUnoptimized(BooleanOp op, VoxelShape shape, VoxelShape... shapes) {
        var result = shape;

        for(var other : shapes) {
            result = Shapes.joinUnoptimized(result, other, op);
        }

        return result;
    }

    static VoxelShape joinUnoptimized(VoxelShape shape, VoxelShape... shapes) {
        return joinUnoptimized(BooleanOp.OR, shape, shapes);
    }

    static VoxelShape join(BooleanOp op, VoxelShape shape, VoxelShape... shapes) {
        return joinUnoptimized(op, shape, shapes).optimize();
    }

    static VoxelShape join(VoxelShape shape, VoxelShape... shapes) {
        return joinUnoptimized(shape, shapes).optimize();
    }

    static VoxelShape rotateHorizontal(VoxelShape shape, Vec3 center, Direction facing) {
        return switch (facing) {
            case EAST -> Shapes.rotate(shape, OctahedralGroup.BLOCK_ROT_Z_90, center);
            case SOUTH -> Shapes.rotate(shape, OctahedralGroup.BLOCK_ROT_Z_180, center);
            case WEST -> Shapes.rotate(shape, OctahedralGroup.BLOCK_ROT_Z_270, center);
            default -> shape;
        };
    }

    static VoxelShape rotateHorizontal(VoxelShape shape, Direction facing) {
        return rotateHorizontal(shape, Shapes.BLOCK_CENTER, facing);
    }
}
