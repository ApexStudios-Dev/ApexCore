package dev.apexstudios.apexcore.extension;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
public interface ApexOctahedralGroup {
    Direction.Axis[] AXES = Direction.Axis.values();

    default Direction.Axis permute(Direction.Axis axis) {
        return AXES[((OctahedralGroup) this).permutation.permutation(axis.ordinal())];
    }

    static OctahedralGroup fromAngles(int a, int b) {
        a = Mth.positiveModulo(a, 360);
        b = Mth.positiveModulo(b, 360);

        if(a % 90 == 0 && b % 90 == 0) {
            var group = OctahedralGroup.IDENTITY;

            for(var i = 0; i < b; i += 90) {
                group = group.compose(OctahedralGroup.ROT_90_Y_NEG);
            }

            for(var i = 0; i < a; i += 90) {
                group = group.compose(OctahedralGroup.ROT_90_X_NEG);
            }

            return group;
        }

        throw new IllegalArgumentException("Angles must be divisible by 90");
    }
}
