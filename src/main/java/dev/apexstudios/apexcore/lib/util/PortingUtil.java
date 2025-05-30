package dev.apexstudios.apexcore.lib.util;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.ApiStatus;

// TODO: Remove these once neo updates the internal helpers
@ApiStatus.ScheduledForRemoval
public interface PortingUtil {
    static void storeItemHandler(ValueOutput output, ItemStackHandler handler, String key) {
        serializeItemHandler(output.child(key), handler);
    }

    private static void serializeItemHandler(ValueOutput output, ItemStackHandler handler) {
        var itemsTag = output.list("Items", ItemStackWithSlot.CODEC);

        for(var i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);

            if(!stack.isEmpty())
                itemsTag.add(new ItemStackWithSlot(i, stack));
        }

        output.putInt("Size", handler.getSlots());
    }

    static void readItemHandler(ValueInput input, ItemStackHandler handler, String key) {
        readItemHandler(input.childOrEmpty(key), handler);
    }

    private static void readItemHandler(ValueInput input, ItemStackHandler handler) {
        handler.setSize(input.getIntOr("Size", handler.getSlots()));
        input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(stack -> handler.setStackInSlot(stack.slot(), stack.stack()));
        // handler.onLoad();
    }
}
