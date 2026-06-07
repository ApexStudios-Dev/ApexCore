package dev.apexstudios.apexcore.extension;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;

// TODO: Remove when PR is merged
// if not merged for release comment out as to not break other mods
@Experimental
public interface BlockExtension {
    default float getBounceRestitution(BlockState blockState) {
        return self().getBounceRestitution();
    }

    private Block self() {
        return (Block) this;
    }
}
