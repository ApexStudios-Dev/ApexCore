package dev.apexstudios.apexcore.core.data.provider.datamap;

import dev.apexstudios.apexcore.lib.data.provider.datamap.SimpleDataMapBuilder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

final class SimpleDataMapBuilderImpl<TRegistry, TValue> extends DataMapBuilderImpl<TRegistry, TValue, SimpleDataMapBuilder<TRegistry, TValue>> implements SimpleDataMapBuilder<TRegistry, TValue> {
    SimpleDataMapBuilderImpl(DataMapType<TRegistry, TValue> dataMapType) {
        super(dataMapType);
    }
}
