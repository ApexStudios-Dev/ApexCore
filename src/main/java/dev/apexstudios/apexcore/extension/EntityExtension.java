package dev.apexstudios.apexcore.extension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface EntityExtension {
    default double getBlockBounciness(BlockState blockState) {
        var blockBounciness = ((BlockStateExtension) blockState).getBounceRestitution();

        if (!(self() instanceof LivingEntity)) {
            blockBounciness *= .8F;
        }

        return blockBounciness;
    }

    private Entity self() {
        return (Entity) this;
    }
}
