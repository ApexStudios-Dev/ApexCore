package dev.apexstudios.apexcore.mixin;

import dev.apexstudios.apexcore.extension.BlockExtension;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class BlockMixin implements BlockExtension {
}
