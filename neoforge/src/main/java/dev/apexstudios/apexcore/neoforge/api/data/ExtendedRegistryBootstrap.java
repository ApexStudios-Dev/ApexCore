package dev.apexstudios.apexcore.neoforge.api.data;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;

public interface ExtendedRegistryBootstrap<TRegistry> extends HolderGetter<TRegistry> {
    ICondition[] NO_CONDITIONS = new ICondition[0];

    Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, Lifecycle lifecycle, TRegistry value, ICondition... conditions);

    default Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, Lifecycle lifecycle, TRegistry value) {
        return register(registryKey, lifecycle, value, NO_CONDITIONS);
    }

    default Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, TRegistry value, ICondition... conditions) {
        return register(registryKey, Lifecycle.stable(), value, conditions);
    }

    default Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, TRegistry value) {
        return register(registryKey, Lifecycle.stable(), value, NO_CONDITIONS);
    }

    Holder.Reference<TRegistry> register(Identifier registryName, Lifecycle lifecycle, TRegistry value, ICondition... conditions);

    default Holder.Reference<TRegistry> register(Identifier registryName, Lifecycle lifecycle, TRegistry value) {
        return register(registryName, lifecycle, value, NO_CONDITIONS);
    }

    default Holder.Reference<TRegistry> register(Identifier registryName, TRegistry value, ICondition... conditions) {
        return register(registryName, Lifecycle.stable(), value, conditions);
    }

    default Holder.Reference<TRegistry> register(Identifier registryName, TRegistry value) {
        return register(registryName, Lifecycle.stable(), value, NO_CONDITIONS);
    }

    Holder.Reference<TRegistry> register(String identifier, Lifecycle lifecycle, TRegistry value, ICondition... conditions);

    default Holder.Reference<TRegistry> register(String identifier, Lifecycle lifecycle, TRegistry value) {
        return register(identifier, lifecycle, value, NO_CONDITIONS);
    }

    default Holder.Reference<TRegistry> register(String identifier, TRegistry value, ICondition... conditions) {
        return register(identifier, Lifecycle.stable(), value, conditions);
    }

    default Holder.Reference<TRegistry> register(String identifier, TRegistry value) {
        return register(identifier, Lifecycle.stable(), value, NO_CONDITIONS);
    }

    <TOther> HolderGetter<TOther> lookup(ResourceKey<? extends Registry<? extends TOther>> registryType);

    <TOther> Optional<HolderLookup.RegistryLookup<TOther>> registryLookup(ResourceKey<? extends Registry<? extends TOther>> registryType);
}
