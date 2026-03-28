package dev.apexstudios.apexcore.common.data.provider;

import com.google.common.base.Predicates;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.api.data.provider.context.ProviderOutputContext;
import dev.apexstudios.apexcore.api.data.provider.model.ModelProvider;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

public final class ModelProviderImpl implements BaseProvider, ModelProvider {
    private final net.minecraft.client.data.models.ModelProvider.ItemInfoCollector itemInfoCollector = new net.minecraft.client.data.models.ModelProvider.ItemInfoCollector(this::filteredItems);
    private final net.minecraft.client.data.models.ModelProvider.BlockStateGeneratorCollector blockStateGeneratorCollector = new net.minecraft.client.data.models.ModelProvider.BlockStateGeneratorCollector(this::filteredBlocks);
    private final net.minecraft.client.data.models.ModelProvider.SimpleModelCollector simpleModelCollector = new net.minecraft.client.data.models.ModelProvider.SimpleModelCollector();
    private final BlockModelGenerators blockModels = new BlockModelGenerators(blockStateGeneratorCollector, itemInfoCollector, simpleModelCollector);
    private final ItemModelGenerators itemModels = new ItemModelGenerators(itemInfoCollector, simpleModelCollector);
    private final String modId;
    private Supplier<Stream<? extends Holder<Item>>> knownItemsSupplier = BuiltInRegistries.ITEM::listElements;
    private Supplier<Stream<? extends Holder<Block>>> knownBlocksSupplier = BuiltInRegistries.BLOCK::listElements;
    private Predicate<Holder<Item>> itemFilter = Predicates.alwaysTrue();
    private Predicate<Holder<Block>> blockFilter = Predicates.alwaysTrue();

    @ApiStatus.Internal
    public ModelProviderImpl(ProviderListenerContext context) {
        modId = context.modId();
    }

    private Stream<? extends Holder<Item>> filteredItems() {
        return knownItemsSupplier.get()
                .filter(holder -> holder.unwrapKey().orElseThrow().identifier().getNamespace().equals(modId))
                .filter(itemFilter);
    }

    private Stream<? extends Holder<Block>> filteredBlocks() {
        return knownBlocksSupplier.get()
                .filter(holder -> holder.unwrapKey().orElseThrow().identifier().getNamespace().equals(modId))
                .filter(blockFilter);
    }

    @Override
    public CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context) {
        blockStateGeneratorCollector.validate();
        itemInfoCollector.finalizeAndValidate();

        return CompletableFuture.allOf(
                blockStateGeneratorCollector.save(cache, context.pathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates")),
                simpleModelCollector.save(cache, context.pathProvider(PackOutput.Target.RESOURCE_PACK, "models")),
                itemInfoCollector.save(cache, context.pathProvider(PackOutput.Target.RESOURCE_PACK, "items"))
        );
    }

    @Override
    public ModelProvider knownItems(Supplier<Stream<? extends Holder<Item>>> knownItemsSupplier) {
        this.knownItemsSupplier = knownItemsSupplier;
        return this;
    }

    @Override
    public ModelProvider knownBlocks(Supplier<Stream<? extends Holder<Block>>> knownBlocksSupplier) {
        this.knownBlocksSupplier = knownBlocksSupplier;
        return this;
    }

    @Override
    public ModelProvider itemFilter(Predicate<Holder<Item>> itemFilter) {
        this.itemFilter = this.itemFilter.and(itemFilter);
        return this;
    }

    @Override
    public ModelProvider blockFilter(Predicate<Holder<Block>> blockFilter) {
        this.blockFilter = this.blockFilter.and(blockFilter);
        return this;
    }

    @Override
    public BlockModelGenerators blockModels() {
        return blockModels;
    }

    @Override
    public ItemModelGenerators itemModels() {
        return itemModels;
    }
}
