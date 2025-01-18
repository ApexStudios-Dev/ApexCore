package dev.apexstudios.apexcore.lib.tooltip;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TooltipMutator {
    void accept(ItemStack stack, Item.TooltipContext context, Consumer<Component> adder, @Nullable Player player, TooltipFlag flag);

    static TooltipMutator forProvider(TooltipProvider provider) {
        return (stack, context, adder, player, flag) -> provider.addToTooltip(context, adder, flag);
    }

    static <TComponent extends TooltipProvider> TooltipMutator forComponent(DataComponentType<TComponent> componentType) {
        return (stack, context, adder, player, flag) -> stack.addToTooltip(componentType, context, adder, flag);
    }
}
