package dev.apexstudios.apexcore.mixin;

import dev.apexstudios.apexcore.lib.util.shapes.ApexAxis;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
@Mixin(Direction.Axis.class)
public class AxisMixin implements ApexAxis { }
