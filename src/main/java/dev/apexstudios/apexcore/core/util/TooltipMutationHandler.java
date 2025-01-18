package dev.apexstudios.apexcore.core.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.apexstudios.apexcore.lib.tooltip.RegisterTooltipEvent;
import dev.apexstudios.apexcore.lib.tooltip.TooltipMutator;
import dev.apexstudios.apexcore.lib.tooltip.TooltipOrder;
import dev.apexstudios.apexcore.lib.tooltip.TooltipPosition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class TooltipMutationHandler {
    private static final TooltipMutator NOTHING = (stack, context, adder, player, flag) -> { };
    private static final Table<TooltipPosition, TooltipOrder, TooltipMutator> MUTATORS = HashBasedTable.create();

    public static TooltipMutator mutate(TooltipPosition position, TooltipOrder order) {
        var mutator = MUTATORS.get(position, order);
        return mutator == null ? NOTHING : mutator;
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(() -> {
            var table = RegisterTooltipEvent.post();

            for(var position : table.rowKeySet()) {
                var row = table.row(position);

                for(var order : row.keySet()) {
                    var list = row.get(order);

                    if(list != null && !list.isEmpty()) {
                        var merged = list.stream().reduce(NOTHING, TooltipMutationHandler::merge);
                        MUTATORS.put(position, order, merged);
                    }
                }
            }
        }));
    }

    private static TooltipMutator merge(TooltipMutator before, TooltipMutator after) {
        return (stack, context, adder, player, flag) -> {
            before.accept(stack, context, adder, player, flag);
            after.accept(stack, context, adder, player, flag);
        };
    }
}
