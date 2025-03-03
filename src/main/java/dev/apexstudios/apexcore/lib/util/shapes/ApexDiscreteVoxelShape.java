package dev.apexstudios.apexcore.lib.util.shapes;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
public interface ApexDiscreteVoxelShape {
    static DiscreteVoxelShape rotate(DiscreteVoxelShape voxelShape, OctahedralGroup group) {
        if(group == OctahedralGroup.IDENTITY)
            return voxelShape;

        var axisX = ApexOctahedralGroup.permute(group,Direction.Axis.X);
        var axisY = ApexOctahedralGroup.permute(group,Direction.Axis.Y);
        var axisZ = ApexOctahedralGroup.permute(group,Direction.Axis.Z);

        var sizeX = axisX.choose(voxelShape.xSize, voxelShape.ySize, voxelShape.zSize);
        var sizeY = axisY.choose(voxelShape.xSize, voxelShape.ySize, voxelShape.zSize);
        var sizeZ = axisZ.choose(voxelShape.xSize, voxelShape.ySize, voxelShape.zSize);

        var invertX = group.inverts(axisX);
        var invertY = group.inverts(axisY);
        var invertZ = group.inverts(axisZ);

        var choosenX = ApexAxis.choose(axisX, invertX, invertY, invertZ);
        var choosenY = ApexAxis.choose(axisY, invertX, invertY, invertZ);
        var choosenZ = ApexAxis.choose(axisZ, invertX, invertY, invertZ);

        var shape = new BitSetDiscreteVoxelShape(sizeX, sizeY, sizeZ);

        for(var x = 0; x < voxelShape.xSize; x++) {
            for(var y = 0; y < voxelShape.ySize; y++) {
                for(var z = 0; z < voxelShape.zSize; z++) {
                    if(voxelShape.isFull(x, y, z)) {
                        var x1 = axisX.choose(x, y, z);
                        var y1 = axisY.choose(x, y, z);
                        var z1 = axisZ.choose(x, y, z);

                        shape.fill(
                                choosenX ? sizeX - 1 - x1 : x1,
                                choosenY ? sizeY - 1 - y1 : y1,
                                choosenZ ? sizeZ - 1 - z1 : z1
                        );
                    }
                }
            }
        }

        return shape;
    }
}
