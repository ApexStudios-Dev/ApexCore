package dev.apexstudios.apexcore.mixin;

import dev.apexstudios.apexcore.extension.ApexDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
@Mixin(DiscreteVoxelShape.class)
public class DiscreteVoxelShapeMixin implements ApexDiscreteVoxelShape { }
