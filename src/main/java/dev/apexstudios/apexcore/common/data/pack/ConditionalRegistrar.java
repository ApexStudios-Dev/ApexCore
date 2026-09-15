package dev.apexstudios.apexcore.common.data.pack;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;

@FunctionalInterface
public interface ConditionalRegistrar<TRegistry> {
    void accept(ResourceKey<TRegistry> registryKey, ICondition[] conditions);
}
