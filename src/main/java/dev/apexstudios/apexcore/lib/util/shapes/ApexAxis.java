package dev.apexstudios.apexcore.lib.util.shapes;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
public interface ApexAxis {
    static boolean choose(Direction.Axis axis, boolean x, boolean y, boolean z) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }
}
