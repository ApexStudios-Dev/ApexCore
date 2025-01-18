package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import org.jetbrains.annotations.Nullable;

public interface DelegatedWorldGenLevel extends DelegatedServerLevelAccessor, WorldGenLevel {
    @Override
    WorldGenLevel delegate();

    @Override
    default long getSeed() {
        return delegate().getSeed();
    }

    @Override
    default boolean ensureCanWrite(BlockPos pos) {
        return delegate().ensureCanWrite(pos);
    }

    @Override
    default void setCurrentlyGenerating(@Nullable Supplier<String> currentlyGenerating) {
        delegate().setCurrentlyGenerating(currentlyGenerating);
    }
}
