package dev.apexstudios.apexcore.lib.util.shapes;

import com.google.common.collect.Maps;
import com.mojang.math.OctahedralGroup;
import dev.apexstudios.apexcore.extension.ApexOctahedralGroup;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;

public interface ApexShapes {
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    Vec3 BLOCK_CENTER = new Vec3(.5D, .5D, .5D);

    static VoxelShape join(VoxelShape shape, VoxelShape... shapes) {
        var result = shape;

        for(var other : shapes) {
            result = Shapes.joinUnoptimized(result, other, BooleanOp.OR);
        }

        return result.optimize();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static VoxelShape rotate(VoxelShape shape, OctahedralGroup group) {
        return rotate(shape, group, BLOCK_CENTER);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static VoxelShape rotate(VoxelShape shape, OctahedralGroup group, Vec3 center) {
        if(group == OctahedralGroup.IDENTITY)
            return shape;

        var discrete = shape.shape.rotate(group);

        if(shape instanceof CubeVoxelShape && BLOCK_CENTER.equals(center))
            return new CubeVoxelShape(discrete);

        var axisX = group.permute(Direction.Axis.X);
        var axisY = group.permute(Direction.Axis.Y);
        var axisZ = group.permute(Direction.Axis.Z);

        var xCoords = shape.getCoords(axisX);
        var yCoords = shape.getCoords(axisY);
        var zCoords = shape.getCoords(axisZ);

        var invertX = group.inverts(axisX);
        var invertY = group.inverts(axisY);
        var invertZ = group.inverts(axisZ);

        var chosenX = axisX.choose(invertX, invertY, invertZ);
        var chosenY = axisY.choose(invertX, invertY, invertZ);
        var chosenZ = axisZ.choose(invertX, invertY, invertZ);

        return new ArrayVoxelShape(
                discrete,
                makeAxis(xCoords, chosenX, center.get(axisX), center.x()),
                makeAxis(yCoords, chosenY, center.get(axisY), center.y()),
                makeAxis(zCoords, chosenZ, center.get(axisZ), center.z())
        );
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static DoubleList makeAxis(DoubleList list, boolean flag, double x, double y) {
        if(!flag && x == y)
            return list;

        var size = list.size();
        var result = new DoubleArrayList(size);
        var j = flag ? -1 : 1;

        for(var k = flag ? size - 1 : 0; k >= 0 && k < size; k += j) {
            result.add(y + (double) j * (list.getDouble(k) - x));
        }

        return result;
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction.Axis, VoxelShape> rotateHorizontalAxis(VoxelShape shape) {
        return rotateHorizontalAxis(shape, BLOCK_CENTER);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction.Axis, VoxelShape> rotateHorizontalAxis(VoxelShape shape, Vec3 center) {
        return Maps.newEnumMap(Map.of(
                Direction.Axis.Z, shape,
                Direction.Axis.X, rotate(shape, ApexOctahedralGroup.fromAngles(0, 90), center)
        ));
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction.Axis, VoxelShape> rotateAllAxis(VoxelShape shape) {
        return rotateAllAxis(shape, BLOCK_CENTER);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction.Axis, VoxelShape> rotateAllAxis(VoxelShape shape, Vec3 center) {
        return Maps.newEnumMap(Map.of(
                Direction.Axis.Z, shape,
                Direction.Axis.X, rotate(shape, ApexOctahedralGroup.fromAngles(0, 90), center),
                Direction.Axis.Y, rotate(shape, ApexOctahedralGroup.fromAngles(90, 0), center)
        ));
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape shape) {
        return rotateHorizontal(shape, BLOCK_CENTER);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape shape, Vec3 center) {
        return Maps.newEnumMap(Map.of(
                Direction.NORTH, shape,
                Direction.EAST, rotate(shape, ApexOctahedralGroup.fromAngles(0, 90), center),
                Direction.SOUTH, rotate(shape, ApexOctahedralGroup.fromAngles(0, 180), center),
                Direction.WEST, rotate(shape, ApexOctahedralGroup.fromAngles(0, 270), center)
        ));
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction, VoxelShape> rotateAll(VoxelShape shape) {
        return rotateAll(shape, BLOCK_CENTER);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<Direction, VoxelShape> rotateAll(VoxelShape shape, Vec3 center) {
        return Maps.newEnumMap(Map.of(
                Direction.NORTH, shape,
                Direction.EAST, rotate(shape, ApexOctahedralGroup.fromAngles(0, 90), center),
                Direction.SOUTH, rotate(shape, ApexOctahedralGroup.fromAngles(0, 180), center),
                Direction.WEST, rotate(shape, ApexOctahedralGroup.fromAngles(0, 270), center),
                Direction.UP, rotate(shape, ApexOctahedralGroup.fromAngles(270, 0), center),
                Direction.DOWN, rotate(shape, ApexOctahedralGroup.fromAngles(90, 0), center)
        ));
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
    @Deprecated(forRemoval = true, since = "1.21.4")
    static Map<AttachFace, Map<Direction, VoxelShape>> rotateAttachFace(VoxelShape shape) {
        return Map.of(
                AttachFace.WALL, rotateHorizontal(shape),
                AttachFace.FLOOR, rotateHorizontal(rotate(shape, ApexOctahedralGroup.fromAngles(270, 0))),
                AttachFace.CEILING, rotateHorizontal(rotate(shape, ApexOctahedralGroup.fromAngles(90, 180)))
        );
    }
}
