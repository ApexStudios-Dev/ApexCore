package dev.apexstudios.apexcore.core.data.provider.loot;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.core.data.provider.BaseProvider;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderOutputContext;
import dev.apexstudios.apexcore.lib.data.provider.loot.LootTableProvider;
import dev.apexstudios.apexcore.lib.data.provider.loot.LootTableSubProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class LootTableProviderImpl implements BaseProvider, LootTableProvider {
    public static final ProviderType<LootTableProvider> PROVIDER_TYPE = ProviderType.register(ApexCore.identifier("loot_tables"), LootTableProviderImpl::new);

    private final Map<ResourceKey<? extends Registry<?>>, Supplier<? extends Stream<? extends Holder<?>>>> knownElements = Maps.newHashMap();
    private final Map<ResourceKey<? extends Registry<?>>, Predicate<Holder<?>>> elementFilters = Maps.newHashMap();
    private final Multimap<ContextKeySet, Consumer<? extends LootTableSubProvider>> subProvidersListeners = MultimapBuilder.hashKeys().linkedHashSetValues().build();
    private final Set<ResourceKey<LootTable>> requiredLootTables = Sets.newHashSet();

    private final Map<ContextKeySet, LootTableSubProviderFactory> subProviders = Map.of(
            LootContextParamSets.BLOCK, BlockLootSubProviderImpl::new,
            LootContextParamSets.ENTITY, EntityLootSubProviderImpl::new
    );

    private LootTableProviderImpl() {

    }

    private <TRegistry> Stream<? extends Holder<TRegistry>> filteredElements(ProviderListenerContext context, ResourceKey<? extends Registry<TRegistry>> registryType) {
        var stream = knownElements.computeIfAbsent(registryType, $ -> () -> context.registries().lookupOrThrow(registryType).listElements());
        var filter = elementFilters.computeIfAbsent(registryType, $ -> Predicates.alwaysTrue());
        return stream.get().map(holder -> (Holder<TRegistry>) holder).filter(filter).filter(holder -> !(holder.value() instanceof FeatureElement element) || element.isEnabled(context.enabledFeatures()));
    }

    @Override
    public CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context) {
        var factoryContext = new FactoryContextImpl(context);

        subProvidersListeners.keySet().forEach(lootContext -> {
            var subProvider = subProviders.getOrDefault(lootContext, LootTableSubProviderImpl::new).create(factoryContext);
            subProvidersListeners.get(lootContext).forEach(listener -> ((Consumer<LootTableSubProvider>) listener).accept(subProvider));
        });

        factoryContext.registry.freeze();
        var problemCollector = new ProblemReporter.Collector();
        var registryAccess = new RegistryAccess.ImmutableRegistryAccess(List.of(factoryContext.registry)).freeze();
        var validationContext = new ValidationContext(problemCollector, LootContextParamSets.ALL_PARAMS, registryAccess);

        for(var lootTable : Sets.difference(requiredLootTables, factoryContext.registry.registryKeySet())) {
            problemCollector.report("Missing built-in loot table: " + lootTable.location());
        }

        factoryContext.registry.listElements().forEach(holder -> {
            var lootTable = holder.value();
            var lootTableKey = holder.key();
            lootTable.validate(validationContext.setContextKeySet(lootTable.getParamSet()).enterElement('{' + lootTableKey.location().toString() + '}', lootTableKey));
        });

        var problems = problemCollector.get();

        if(!problems.isEmpty()) {
            var logger = LogUtils.getLogger();
            problems.forEach((path, problem) -> logger.warn("Found validation problem in {}: {}", path, problem));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }

        var pathProvider = context.elementPathProvider(Registries.LOOT_TABLE);
        var result = factoryContext.registry.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().location(), Map.Entry::getValue));
        return BaseProvider.saveAll(cache, LootTable.DIRECT_CODEC, pathProvider, result);
    }

    @Override
    public LootTableProvider forContext(ContextKeySet context, Consumer<? extends LootTableSubProvider> consumer) {
        subProvidersListeners.put(context, consumer);
        return this;
    }

    @Override
    public LootTableProvider requiredLootTable(ResourceKey<LootTable> lootTable) {
        requiredLootTables.add(lootTable);
        return this;
    }

    @Override
    public <TRegistry> LootTableProvider knownElements(ResourceKey<? extends Registry<TRegistry>> registryType, Supplier<Stream<? extends Holder<TRegistry>>> knownElementsSupplier) {
        knownElements.put(registryType, knownElementsSupplier);
        return this;
    }

    @Override
    public <TRegistry> LootTableProvider elementFilter(ResourceKey<? extends Registry<TRegistry>> registryType, Predicate<Holder<TRegistry>> filter) {
        elementFilters.merge(registryType, (Predicate) filter, Predicate::and);
        return this;
    }

    private final class FactoryContextImpl implements LootTableSubProviderFactory.Context {
        private final ProviderListenerContext context;
        private final Registry<LootTable> registry = new MappedRegistry<>(Registries.LOOT_TABLE, Lifecycle.experimental());
        private final Map<RandomSupport.Seed128bit, ResourceLocation> sequnceMap = new Object2ObjectOpenHashMap<>();

        private FactoryContextImpl(ProviderListenerContext context) {
            this.context = context;
        }

        @Override
        public void accept(ResourceKey<LootTable> lootTableKey, Supplier<LootTable.Builder> lootTableBuilder) {
            var sequenceId = lootTableKey.location();
            var existingSequenceId = sequnceMap.put(RandomSequence.seedForKey(sequenceId), sequenceId);

            if (existingSequenceId != null)
                Util.logAndPauseIfInIde("LootTable random sequence seed collision on " + existingSequenceId + " and " + lootTableKey.location());

            var lootTable = lootTableBuilder.get().setRandomSequence(sequenceId).build();
            Registry.register(registry, lootTableKey, lootTable);
        }

        @Override
        public <TRegistry> Stream<? extends Holder<TRegistry>> knownElements(ResourceKey<? extends Registry<TRegistry>> registryType) {
            return LootTableProviderImpl.this.filteredElements(context, registryType);
        }

        @Override
        public HolderLookup.Provider registries() {
            return context.registries();
        }

        @Override
        public String modId() {
            return context.modId();
        }

        @Override
        public ResourceManager getResourceManager(PackType packType) {
            return context.getResourceManager(packType);
        }

        @Override
        public FeatureFlagSet enabledFeatures() {
            return context.enabledFeatures();
        }
    }
}
