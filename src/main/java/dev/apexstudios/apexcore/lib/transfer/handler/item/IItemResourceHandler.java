package dev.apexstudios.apexcore.lib.transfer.handler.item;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.transfer.handler.IResourceHandler;
import dev.apexstudios.apexcore.lib.transfer.resource.item.ItemResource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public interface IItemResourceHandler extends IResourceHandler<ItemResource> {
    default int getDefaultCapacity(int index) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    @Override
    default int getCapacity(int index, ItemResource resource) {
        if(resource.isEmpty())
            return getDefaultCapacity(index);

        return Math.min(getDefaultCapacity(index), resource.getMaxStackSize());
    }

    default IItemHandler toLegacy() {
        return new LegacyItemHandler(this);
    }

    interface Capability {
        ResourceLocation ID = ApexCore.identifier("item_resource_handler");

        BlockCapability<IItemResourceHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(ID, IItemResourceHandler.class);

        static void registerBlockWithLegacyFallback(RegisterCapabilitiesEvent event, IBlockCapabilityProvider<IItemResourceHandler, @Nullable Direction> provider, Block... blocks) {
            event.registerBlock(BLOCK, provider, blocks);

            event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, blockState, blockEntity, context) -> {
                var itemHandler = provider.getCapability(level, pos, blockState, blockEntity, context);
                return itemHandler == null ? null : itemHandler.toLegacy();
            }, blocks);
        }

        static <T extends BlockEntity> void registerBlockEntityWithLegacyFallback(RegisterCapabilitiesEvent event, BlockEntityType<T> blockEntityType, ICapabilityProvider<T, @Nullable Direction, IItemResourceHandler> provider) {
            event.registerBlockEntity(BLOCK, blockEntityType, provider);

            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, blockEntityType, (blockEntity, context) -> {
                var itemHandler = provider.getCapability(blockEntity, context);
                return itemHandler == null ? null : itemHandler.toLegacy();
            });
        }
    }
}
