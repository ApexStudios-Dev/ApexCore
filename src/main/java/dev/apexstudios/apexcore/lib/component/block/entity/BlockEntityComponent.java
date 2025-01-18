package dev.apexstudios.apexcore.lib.component.block.entity;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.block.BlockEvents;
import dev.apexstudios.apexcore.lib.component.Component;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityComponent extends Component<BlockEntityComponent>, ComponentHolder<BlockEntityComponent>, BlockEvents {
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
    default void applyImplicitComponents(BlockEntity.DataComponentInput input) {

    }

    @ForOverride
    default void collectImplicitComponents(DataComponentMap.Builder builder) {

    }

    @ForOverride
    default void removeComponentsFromTag(CompoundTag tag) {

    }
}
