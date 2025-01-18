package dev.apexstudios.apexcore.mixin;

import com.mojang.math.OctahedralGroup;
import dev.apexstudios.apexcore.lib.util.shapes.ApexOctahedralGroup;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
@Mixin(OctahedralGroup.class)
public class OctahedralGroupMixin implements ApexOctahedralGroup { }
