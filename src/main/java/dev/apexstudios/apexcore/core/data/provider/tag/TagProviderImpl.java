package dev.apexstudios.apexcore.core.data.provider.tag;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.core.data.provider.BaseProvider;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderOutputContext;
import dev.apexstudios.apexcore.lib.data.provider.tag.TagBuilder;
import dev.apexstudios.apexcore.lib.data.provider.tag.TagProvider;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;

sealed class TagProviderImpl<TRegistry, TBuilder extends TagBuilder<TRegistry, TBuilder>> implements TagProvider<TRegistry, TBuilder>, BaseProvider permits IntrusiveTagProviderImpl, SimpleTagProviderImpl {
    protected final ResourceKey<? extends Registry<TRegistry>> registryType;
    protected final String namespace;
    private final Map<TagKey<TRegistry>, TBuilder> builders = Maps.newHashMap();
    private final Function<String, TBuilder> builderFactory;

    protected TagProviderImpl(ProviderListenerContext context, ResourceKey<? extends Registry<TRegistry>> registryType, Function<String, TBuilder> builderFactory) {
        this.registryType = registryType;
        this.builderFactory = builderFactory;

        namespace = context.modId();
    }

    @Override
    public TBuilder tag(TagKey<TRegistry> tag) {
        return builders.computeIfAbsent(tag, $ -> builderFactory.apply(namespace));
    }

    @Override
    public TBuilder tag(ResourceKey<TRegistry> registryKey) {
        return tag(TagKey.create(registryType, registryKey.identifier()));
    }

    @Override
    public TBuilder tag(Identifier registryName) {
        return tag(ResourceKey.create(registryType, registryName));
    }

    @Override
    public TBuilder tag(String tagPath) {
        return tag(Identifier.fromNamespaceAndPath(namespace, tagPath));
    }

    @Override
    public CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context) {
        return BaseProvider.saveAll(
                cache,
                TagFile.CODEC,
                context.tagPathProvider(registryType),
                builders.entrySet().stream().collect(Collectors.toMap(
                        entry -> entry.getKey().location(),
                        entry -> ((TagBuilderImpl<TRegistry, TBuilder>) entry.getValue()).compile()
                ))
        );
    }
}
