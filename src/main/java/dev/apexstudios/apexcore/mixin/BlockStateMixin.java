package dev.apexstudios.apexcore.mixin;

import dev.apexstudios.apexcore.extension.BlockStateExtension;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public class BlockStateMixin implements BlockStateExtension {
}
