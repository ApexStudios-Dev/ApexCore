package dev.apexstudios.apexcore.api.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.common.data.provider.tag.ItemTagProviderImpl;
import net.minecraft.references.BlockItemId;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.world.item.Item;

public interface ItemTagProvider extends IntrusiveTagProvider<Item, ItemTagBuilder> {
    ProviderType<ItemTagProvider> PROVIDER_TYPE = ItemTagProviderImpl.PROVIDER_TYPE;

    default ItemTagBuilder tag(BlockItemTagId tag) {
        return tag(tag.item());
    }

    default ItemTagBuilder tag(BlockItemId blockItemId) {
        return tag(blockItemId.item());
    }
}
