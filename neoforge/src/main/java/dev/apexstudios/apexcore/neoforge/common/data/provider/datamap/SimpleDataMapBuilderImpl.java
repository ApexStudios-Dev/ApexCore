package dev.apexstudios.apexcore.neoforge.common.data.provider.datamap;

import dev.apexstudios.apexcore.neoforge.api.data.provider.datamap.SimpleDataMapBuilder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

final class SimpleDataMapBuilderImpl<TRegistry, TValue> extends DataMapBuilderImpl<TRegistry, TValue, SimpleDataMapBuilder<TRegistry, TValue>> implements SimpleDataMapBuilder<TRegistry, TValue> {
    SimpleDataMapBuilderImpl(DataMapType<TRegistry, TValue> dataMapType) {
        super(dataMapType);
    }
}
