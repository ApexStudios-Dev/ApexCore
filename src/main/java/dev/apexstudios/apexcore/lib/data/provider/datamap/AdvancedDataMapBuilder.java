package dev.apexstudios.apexcore.lib.data.provider.datamap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

public interface AdvancedDataMapBuilder<TRegistry, TValue, TRemover extends DataMapValueRemover<TRegistry, TValue>> extends DataMapBuilder<TRegistry, TValue, AdvancedDataMapBuilder<TRegistry, TValue, TRemover>> {
    AdvancedDataMapBuilder<TRegistry, TValue, TRemover> remove(ResourceKey<TRegistry> registryKey, TRemover remover);

    AdvancedDataMapBuilder<TRegistry, TValue, TRemover> remove(ResourceLocation registryName, TRemover remover);

    default AdvancedDataMapBuilder<TRegistry, TValue, TRemover> remove(Holder<TRegistry> holder, TRemover remover) {
        return remove(holder.getKey(), remover);
    }

    AdvancedDataMapBuilder<TRegistry, TValue, TRemover> remove(TagKey<TRegistry> tag, TRemover remover);
}
