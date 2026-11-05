package dev.apexstudios.apexcore.extensions;

import dev.apexstudios.apexcore.api.FakeItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemExtensions {
    /// @see net.neoforged.neoforge.common.extensions.IItemExtension#onEntityItemUpdate(ItemStack, ItemEntity)
    default boolean onEntityItemUpdate(ItemStack stack, FakeItemEntity entity) {
        return false;
    }

    /// @see net.neoforged.neoforge.common.extensions.IItemExtension#onDestroyed(ItemEntity, DamageSource)
    default void onDestroyed(FakeItemEntity entity, DamageSource damageSource) {
        // self().onDestroyed(entity);
    }

    private Item self() {
        return (Item) this;
    }
}
