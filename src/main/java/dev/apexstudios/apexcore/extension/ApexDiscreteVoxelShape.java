package dev.apexstudios.apexcore.extension;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
public interface ApexDiscreteVoxelShape {
    default DiscreteVoxelShape rotate(OctahedralGroup group) {
        var self = (DiscreteVoxelShape) this;

        if(group == OctahedralGroup.IDENTITY)
            return self;

        var axisX = group.permute(Direction.Axis.X);
        var axisY = group.permute(Direction.Axis.Y);
        var axisZ = group.permute(Direction.Axis.Z);

        var sizeX = axisX.choose(self.xSize, self.ySize, self.zSize);
        var sizeY = axisY.choose(self.xSize, self.ySize, self.zSize);
        var sizeZ = axisZ.choose(self.xSize, self.ySize, self.zSize);

        var invertX = group.inverts(axisX);
        var invertY = group.inverts(axisY);
        var invertZ = group.inverts(axisZ);

        var choosenX = axisX.choose(invertX, invertY, invertZ);
        var choosenY = axisY.choose(invertX, invertY, invertZ);
        var choosenZ = axisZ.choose(invertX, invertY, invertZ);

        var shape = new BitSetDiscreteVoxelShape(sizeX, sizeY, sizeZ);

        for(var x = 0; x < self.xSize; x++) {
            for(var y = 0; y < self.ySize; y++) {
                for(var z = 0; z < self.zSize; z++) {
                    if(self.isFull(x, y, z)) {
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
