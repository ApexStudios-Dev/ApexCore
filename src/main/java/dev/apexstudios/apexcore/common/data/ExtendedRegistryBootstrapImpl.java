package dev.apexstudios.apexcore.common.data;

import dev.apexstudios.apexcore.api.data.ExtendedRegistryBootstrap;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class ExtendedRegistryBootstrapImpl<TRegistry> implements ExtendedRegistryBootstrap<TRegistry> {
    private final BootstrapContext<TRegistry> delegate;
    private final ResourceKey<? extends Registry<TRegistry>> registryType;
    private final String modId;
    private final HolderGetter<TRegistry> lookup;

    public ExtendedRegistryBootstrapImpl(BootstrapContext<TRegistry> delegate, ResourceKey<? extends Registry<TRegistry>> registryType, String modId) {
        this.delegate = delegate;
        this.registryType = registryType;
        this.modId = modId;

        lookup = delegate.lookup(registryType);
    }

    @Override
    public Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, TRegistry value) {
        return delegate.register(registryKey, value);
    }

    @Override
    public Holder.Reference<TRegistry> register(Identifier registryName, TRegistry value) {
        return register(ResourceKey.create(registryType, registryName), value);
    }

    @Override
    public Holder.Reference<TRegistry> register(String identifier, TRegistry value) {
        return register(Identifier.fromNamespaceAndPath(modId, identifier), value);
    }

    @Override
    public <TOther> HolderGetter<TOther> lookup(ResourceKey<? extends Registry<? extends TOther>> registryType) {
        return delegate.lookup(registryType);
    }

    @Override
    public <TOther> Optional<HolderLookup<TOther>> holderLookup(ResourceKey<? extends Registry<? extends TOther>> registryType) {
        return delegate.holderLookup(registryType);
    }

    @Override
    public Optional<Holder.Reference<TRegistry>> get(ResourceKey<TRegistry> registryKey) {
        return lookup.get(registryKey);
    }

    @Override
    public Holder.Reference<TRegistry> getOrThrow(ResourceKey<TRegistry> registryKey) {
        return lookup.getOrThrow(registryKey);
    }

    @Override
    public Optional<HolderSet.Named<TRegistry>> get(TagKey<TRegistry> tag) {
        return lookup.get(tag);
    }

    @Override
    public HolderSet.Named<TRegistry> getOrThrow(TagKey<TRegistry> tag) {
        return lookup.getOrThrow(tag);
    }
}
