package dev.apexstudios.apexcore.api.data;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface ExtendedRegistryBootstrap<TRegistry> extends HolderGetter<TRegistry> {
    Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, TRegistry value);

    Holder.Reference<TRegistry> register(Identifier registryName, TRegistry value);

    Holder.Reference<TRegistry> register(String identifier, TRegistry value);

    <TOther> HolderGetter<TOther> lookup(ResourceKey<? extends Registry<? extends TOther>> registryType);

    <TOther> Optional<HolderLookup<TOther>> holderLookup(ResourceKey<? extends Registry<? extends TOther>> registryType);
}
