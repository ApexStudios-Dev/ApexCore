package dev.apexstudios.apexcore.api.data.provider.model;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface ModelProvider {
    ModelProvider knownItems(Supplier<Stream<? extends Holder<Item>>> knownItemsSupplier);

    ModelProvider knownBlocks(Supplier<Stream<? extends Holder<Block>>> knownBlocksSupplier);

    ModelProvider itemFilter(Predicate<Holder<Item>> itemFilter);

    ModelProvider blockFilter(Predicate<Holder<Block>> blockFilter);

    default ModelProvider noItems() {
        return knownItems(Stream::empty);
    }

    default ModelProvider blockItems() {
        return itemFilter(holder -> holder.value() instanceof BlockItem);
    }

    default ModelProvider noBlocks() {
        return knownBlocks(Stream::empty);
    }

    default ModelProvider knownItems(DeferredHolder<Item, ?>... items) {
        return knownItems(() -> Stream.of(items));
    }

    default ModelProvider knownBlocks(DeferredHolder<Block, ?>... blocks) {
        return knownBlocks(() -> Stream.of(blocks));
    }

    BlockModelGenerators blockModels();

    ItemModelGenerators itemModels();
}
