package dev.apexstudios.apexcore.lib.tooltip;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import java.util.List;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

public final class RegisterTooltipEvent extends Event implements IModBusEvent {
    private final Table<TooltipPosition, TooltipOrder, List<TooltipMutator>> mutators = HashBasedTable.create();

    private RegisterTooltipEvent() {

    }

    public void registerBefore(TooltipPosition position, TooltipMutator mutator) {
        register(position, TooltipOrder.BEFORE, mutator);
    }

    public void registerAfter(TooltipPosition position, TooltipMutator mutator) {
        register(position, TooltipOrder.AFTER, mutator);
    }

    private void register(TooltipPosition position, TooltipOrder order, TooltipMutator mutator) {
        var list = mutators.get(position, order);

        if(list == null) {
            list = Lists.newLinkedList();
            mutators.put(position, order, list);
        }

        list.add(mutator);
    }

    @ApiStatus.Internal
    public static Table<TooltipPosition, TooltipOrder, List<TooltipMutator>> post() {
        var event = new RegisterTooltipEvent();
        ModLoader.postEventWrapContainerInModOrder(event);
        return event.mutators;
    }
}
