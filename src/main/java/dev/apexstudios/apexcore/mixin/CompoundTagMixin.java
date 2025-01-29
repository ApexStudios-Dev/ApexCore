package dev.apexstudios.apexcore.mixin;

import dev.apexstudios.apexcore.extension.ApexCompoundTag;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
@Mixin(CompoundTag.class)
public class CompoundTagMixin implements ApexCompoundTag {}
