package dev.apexstudios.apexcore.api.ghost;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

public final class RegisterGhostPlacementExtractorsEvent extends Event implements IModBusEvent {
    private final Map<Item, GhostPlacementExtractor> registry;

    @ApiStatus.Internal
    public RegisterGhostPlacementExtractorsEvent(Map<Item, GhostPlacementExtractor> registry) {
        this.registry = registry;
    }

    @SuppressWarnings("deprecation")
    public void registerItem(Item item, GhostPlacementExtractor extractor) {
        if(registry.putIfAbsent(item, extractor) != null) {
            throw new IllegalStateException("Duplicate GhostPlacementExtractor registration: " + item.builtInRegistryHolder().key().identifier());
        }
    }

    public void registerItem(Holder<Item> holder, GhostPlacementExtractor extractor) {
        registerItem(holder.value(), extractor);
    }

    public boolean isItemRegistered(Item item) {
        return registry.containsKey(item);
    }

    public boolean isItemRegistered(Holder<Item> holder) {
        return isItemRegistered(holder.value());
    }

    public void registerBlock(Block block, GhostPlacementExtractor extractor) {
        registerItem(block.asItem(), extractor);
    }

    public void registerBlock(Holder<Block> holder, GhostPlacementExtractor extractor) {
        registerBlock(holder.value(), extractor);
    }

    public boolean isBlockRegistered(Block block) {
        return isItemRegistered(block.asItem());
    }

    public boolean isBlockRegistered(Holder<Block> holder) {
        return isBlockRegistered(holder.value());
    }
}
