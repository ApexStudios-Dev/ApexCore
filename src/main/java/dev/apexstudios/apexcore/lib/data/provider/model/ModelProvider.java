package dev.apexstudios.apexcore.lib.data.provider.model;

import dev.apexstudios.apexcore.lib.registree.Registree;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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

    default ModelProvider fromRegistree(Registree registree) {
        return itemsFromRegistree(registree).blocksFromRegistree(registree);
    }

    default ModelProvider itemsFromRegistree(Registree registree) {
        return knownItems(() -> registree.listElements(Registries.ITEM));
    }

    default ModelProvider blocksFromRegistree(Registree registree) {
        return knownBlocks(() -> registree.listElements(Registries.BLOCK));
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
