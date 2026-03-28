package dev.apexstudios.apexcore.neoforge.api.data.provider.datamap;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;

public interface DataMapBuilder<TRegistry, TValue, TSelf extends DataMapBuilder<TRegistry, TValue, TSelf>> {
    TSelf add(ResourceKey<TRegistry> registryKey, TValue value, boolean replace, ICondition... conditions);

    TSelf add(Identifier registryName, TValue value, boolean replace, ICondition... conditions);

    default TSelf add(Holder<TRegistry> holder, TValue value, boolean replace, ICondition... conditions) {
        return add(holder.getKey(), value, replace, conditions);
    }

    TSelf add(TagKey<TRegistry> tag, TValue value, boolean replace, ICondition... conditions);

    TSelf remove(ResourceKey<TRegistry> registryKey);

    TSelf remove(Identifier registryName);

    default TSelf remove(Holder<TRegistry> holder) {
        return remove(holder.getKey());
    }

    TSelf remove(TagKey<TRegistry> tag);

    TSelf replace(boolean replace);

    TSelf conditions(ICondition... conditions);
}
