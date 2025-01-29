package dev.apexstudios.apexcore.extension;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
public interface ApexAxis {
    default boolean choose(boolean x, boolean y, boolean z) {
        return switch ((Direction.Axis) this) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }
}
