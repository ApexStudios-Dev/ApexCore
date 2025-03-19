package dev.apexstudios.apexcore.lib.component.block.entity.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.BaseBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class NameableBlockEntityComponent extends BaseBlockEntityComponent implements Nameable {
    public static final String NBT_CUSTOM_NAME = "CustomName";

    public static final ComponentType<BlockEntityComponent, NameableBlockEntityComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlockEntity(
            ApexCore.identifier("nameable"),
            Builder::new,
            NameableBlockEntityComponent::new
    );

    @Nullable private final Component defaultName;
    @Nullable private Component customName = null;

    private NameableBlockEntityComponent(ComponentHolder<BlockEntityComponent> holder, Builder builder) {
        super(holder);

        defaultName = builder.defaultName;
    }

    public void setCustomName(@Nullable Component customName) {
        this.customName = customName;
        asBlockEntity().setChanged();
    }

    @Override
    public Component getName() {
        return defaultName == null ? asBlockEntity().getBlockState().getBlock().getName() : defaultName;
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    @Override
    public Component getDisplayName() {
        return customName == null ? getName() : customName;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName;
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter getter) {
        customName = getter.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        if(defaultName != null)
            builder.set(DataComponents.ITEM_NAME, defaultName);
        if(customName != null)
            builder.set(DataComponents.CUSTOM_NAME, customName);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(NBT_CUSTOM_NAME);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        customName = BlockEntity.parseCustomNameSafe(tag.get(NBT_CUSTOM_NAME), registries);
    }

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if(customName != null)
            tag.store(NBT_CUSTOM_NAME, ComponentSerialization.CODEC, customName);
    }

    public static final class Builder implements ComponentBuilder {
        @Nullable private Component defaultName = null;

        public Builder defaultName(Component defaultName) {
            this.defaultName = defaultName;
            return this;
        }
    }
}
