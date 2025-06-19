package dev.apexstudios.apexcore.lib.util;

import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.handlers.resources.IIndexModifier;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public interface ResourceUtil {
    // Copy items from 'contents' into some handler using 'setter'
    static void fillFrom(ItemContainerContents contents, IIndexModifier<ItemResource> setter) {
        var slots = contents.getSlots();

        for(var i = 0; i < slots; i++) {
            var stack = contents.getStackInSlot(i);
            setter.set(i, ItemResource.of(stack), stack.getCount());
        }
    }
}
