package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.provider.tag.ItemTagBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

final class ItemTagBuilderImpl extends IntrusiveTagBuilderImpl<Item, ItemTagBuilder> implements ItemTagBuilder {
    ItemTagBuilderImpl(String namespace) {
        super(namespace);
    }

    @Override
    protected ResourceKey<Item> getKey(Item value) {
        return value.builtInRegistryHolder().key();
    }
}
