package dev.apexstudios.apexcore.lib.component.block.entity;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.component.Component;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
public interface BlockEntityComponent extends Component<BlockEntityComponent, BlockEntity>, ComponentHolder<BlockEntityComponent, BlockEntity> {
    @ForOverride
    default void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {

    }

    @ForOverride
    default void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {

    }

    @ForOverride
    default void applyImplicitComponents(DataComponentGetter getter) {

    }

    @ForOverride
    default void collectImplicitComponents(DataComponentMap.Builder builder) {

    }

    @ForOverride
    default void removeComponentsFromTag(CompoundTag tag) {

    }

    @ForOverride
    default void preRemoveSideEffects(BlockPos pos, BlockState blockState) {

    }

    @ForOverride
    default BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        return blockState;
    }

    @ForOverride
    default int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return 0;
    }
}
