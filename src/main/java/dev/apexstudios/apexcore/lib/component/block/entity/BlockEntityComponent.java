package dev.apexstudios.apexcore.lib.component.block.entity;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.block.BlockEvents;
import dev.apexstudios.apexcore.lib.component.Component;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
public interface BlockEntityComponent extends Component<BlockEntityComponent, BlockEntity>, ComponentHolder<BlockEntityComponent, BlockEntity>, BlockEvents {
    @ForOverride
    default void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {

    }

    @ForOverride
    default void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {

    }

    @ForOverride
    default boolean triggerEvent(int id, int event) {
        return false;
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
}
