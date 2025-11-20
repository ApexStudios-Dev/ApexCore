package dev.apexstudios.apexcore.core.data.provider.datamap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DynamicOps;
import dev.apexstudios.apexcore.lib.data.provider.datamap.DataMapBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.registries.datamaps.DataMapEntry;
import net.neoforged.neoforge.registries.datamaps.DataMapFile;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import org.jetbrains.annotations.Nullable;

class DataMapBuilderImpl<TRegistry, TValue, TSelf extends DataMapBuilder<TRegistry, TValue, TSelf>> implements DataMapBuilder<TRegistry, TValue, TSelf> {
    private final Map<Either<TagKey<TRegistry>, ResourceKey<TRegistry>>, Optional<WithConditions<DataMapEntry<TValue>>>> values = Maps.newLinkedHashMap();
    private final List<DataMapEntry.Removal<TValue, TRegistry>> removals = Lists.newArrayList();
    private final DataMapType<TRegistry, TValue> dataMapType;
    private final List<ICondition> conditions = Lists.newArrayList();
    private boolean replace = false;

    DataMapBuilderImpl(DataMapType<TRegistry, TValue> dataMapType) {
        this.dataMapType = dataMapType;
    }

    protected TSelf add(Either<TagKey<TRegistry>, ResourceKey<TRegistry>> key, @Nullable WithConditions<DataMapEntry<TValue>> value) {
        values.put(key, Optional.ofNullable(value));
        return (TSelf) this;
    }

    protected TSelf remove(Either<TagKey<TRegistry>, ResourceKey<TRegistry>> key, @Nullable DataMapValueRemover<TRegistry, TValue> remover) {
        removals.add(new DataMapEntry.Removal<>(key, Optional.ofNullable(remover)));
        return (TSelf) this;
    }

    protected ResourceKey<TRegistry> registryKey(Identifier registryName) {
        return ResourceKey.create(dataMapType.registryKey(), registryName);
    }

    @Override
    public TSelf add(ResourceKey<TRegistry> registryKey, TValue value, boolean replace, ICondition... conditions) {
        return add(Either.right(registryKey), new WithConditions<>(new DataMapEntry<>(value, replace), conditions));
    }

    @Override
    public TSelf add(Identifier registryName, TValue value, boolean replace, ICondition... conditions) {
        return add(registryKey(registryName), value, replace, conditions);
    }

    @Override
    public TSelf add(TagKey<TRegistry> tag, TValue value, boolean replace, ICondition... conditions) {
        return add(Either.left(tag), new WithConditions<>(new DataMapEntry<>(value, replace), conditions));
    }

    @Override
    public TSelf remove(ResourceKey<TRegistry> registryKey) {
        return remove(Either.right(registryKey), null);
    }

    @Override
    public TSelf remove(Identifier registryName) {
        return remove(registryKey(registryName));
    }

    @Override
    public TSelf remove(TagKey<TRegistry> tag) {
        return remove(Either.left(tag), null);
    }

    @Override
    public TSelf replace(boolean replace) {
        this.replace = replace;
        return (TSelf) this;
    }

    @Override
    public TSelf conditions(ICondition... conditions) {
        Collections.addAll(this.conditions, conditions);
        return (TSelf) this;
    }

    JsonElement save(DynamicOps<JsonElement> ops) {
        return ConditionalOps.createConditionalCodecWithConditions(DataMapFile.codec(dataMapType.registryKey(), dataMapType))
                .encodeStart(ops, Optional.of(new WithConditions<>(conditions, new DataMapFile<>(replace, Map.copyOf(values), List.copyOf(removals)))))
                .getOrThrow();
    }
}
