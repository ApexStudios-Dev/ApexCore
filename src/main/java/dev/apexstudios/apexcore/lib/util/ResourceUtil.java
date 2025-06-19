package dev.apexstudios.apexcore.lib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.handlers.resources.IIndexModifier;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.ApiStatus;

public interface ResourceUtil {
    // Copied from 'Containers' updated to support 'IResourceHandler<ItemResource>'
    @ApiStatus.ScheduledForRemoval // TODO: use ItemUtils once variant there are made public
    static void dropContents(Level level, BlockPos pos, IResourceHandler<ItemResource> handler) {
        dropContents(level, pos.getX(), pos.getY(), pos.getZ(), handler);
    }

    // Copied from 'Containers' updated to support 'IResourceHandler<ItemResource>'
    @ApiStatus.ScheduledForRemoval // TODO: use ItemUtils once variant there are made public
    static void dropContents(Level level, double x, double y, double z, IResourceHandler<ItemResource> handler) {
        var size = handler.size();

        for(var i = 0; i < size; i++) {
            var stack = handler.getResource(i).toStack(handler.getAmount(i));
            Containers.dropItemStack(level, x, y, z, stack);
        }
    }

    // Copy items from 'contents' into some handler using 'setter'
    static void fillFrom(ItemContainerContents contents, IIndexModifier<ItemResource> setter) {
        var slots = contents.getSlots();

        for(var i = 0; i < slots; i++) {
            var stack = contents.getStackInSlot(i);
            setter.set(i, ItemResource.of(stack), stack.getCount());
        }
    }
}
