package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.api.data.provider.tag.BlockTagBuilder;
import dev.apexstudios.apexcore.api.data.provider.tag.BlockTagProvider;
import dev.apexstudios.apexcore.api.data.provider.tag.TagProvider;
import dev.apexstudios.apexcore.common.ApexCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public final class BlockTagProviderImpl extends IntrusiveTagProviderImpl<Block, BlockTagBuilder> implements BlockTagProvider {
    public static final ProviderType<BlockTagProvider> PROVIDER_TYPE = TagProvider.register(ApexCore.ID, Registries.BLOCK, BlockTagProviderImpl::new);

    private BlockTagProviderImpl(ProviderListenerContext context) {
        super(context, Registries.BLOCK, BlockTagBuilderImpl::new);
    }
}
