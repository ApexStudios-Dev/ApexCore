package dev.apexstudios.apexcore.extension;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface BlockStateExtension {
    default float getBounceRestitution() {
        return ((BlockExtension) self().getBlock()).getBounceRestitution(self());
    }

    private BlockState self() {
        return (BlockState) this;
    }
}
