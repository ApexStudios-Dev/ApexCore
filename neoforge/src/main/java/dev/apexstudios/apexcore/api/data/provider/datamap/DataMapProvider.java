package dev.apexstudios.apexcore.api.data.provider.datamap;

import dev.apexstudios.apexcore.api.data.ProviderType;
import dev.apexstudios.apexcore.common.data.provider.datamap.DataMapProviderImpl;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

public interface DataMapProvider {
    ProviderType<DataMapProvider> PROVIDER_TYPE = DataMapProviderImpl.PROVIDER_TYPE;

    <TRegistry, TValue> SimpleDataMapBuilder<TRegistry, TValue> builder(DataMapType<TRegistry, TValue> dataMapType);

    <TRegistry, TValue, TRemover extends DataMapValueRemover<TRegistry, TValue>> AdvancedDataMapBuilder<TRegistry, TValue, TRemover> builder(AdvancedDataMapType<TRegistry, TValue, TRemover> dataMapType);
}
