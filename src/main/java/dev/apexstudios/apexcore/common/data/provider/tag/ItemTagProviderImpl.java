package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.api.data.provider.tag.ItemTagBuilder;
import dev.apexstudios.apexcore.api.data.provider.tag.ItemTagProvider;
import dev.apexstudios.apexcore.api.data.provider.tag.TagProvider;
import dev.apexstudios.apexcore.common.ApexCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public final class ItemTagProviderImpl extends IntrusiveTagProviderImpl<Item, ItemTagBuilder> implements ItemTagProvider {
    public static final ProviderType<ItemTagProvider> PROVIDER_TYPE = TagProvider.register(ApexCore.ID, Registries.ITEM, ItemTagProviderImpl::new);

    private ItemTagProviderImpl(ProviderListenerContext context) {
        super(context, Registries.ITEM, ItemTagBuilderImpl::new);
    }
}
