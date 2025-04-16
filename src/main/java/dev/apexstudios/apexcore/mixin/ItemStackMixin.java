package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.core.util.TooltipMutationHandler;
import dev.apexstudios.apexcore.lib.tooltip.TooltipOrder;
import dev.apexstudios.apexcore.lib.tooltip.TooltipPosition;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @WrapOperation(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean ApexCore$appendTooltipHeaderBefore(
            List<Component> lines,
            Object header,
            Operation<Boolean> original,
            @Local(argsOnly = true) Item.TooltipContext context,
            @Local(argsOnly = true) @Nullable Player player,
            @Local(argsOnly = true) TooltipFlag flag
    ) {
        var self = (ItemStack) (Object) this;
        TooltipMutationHandler.mutate(TooltipPosition.HEADER, TooltipOrder.BEFORE).accept(self, context, lines::add, player, flag);
        var result = original.call(lines, header);
        TooltipMutationHandler.mutate(TooltipPosition.HEADER, TooltipOrder.AFTER).accept(self, context, lines::add, player, flag);
        return result;
    }

    @WrapOperation(
            method = "addDetailsToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V"
            )
    )
    private void ApexCore$appendTooltipItemBefore(
            Item item,
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag,
            Operation<Void> original,
            @Local(argsOnly = true) @Nullable Player player
    ) {
        TooltipMutationHandler.mutate(TooltipPosition.ITEM, TooltipOrder.BEFORE).accept(stack, context, adder, player, flag);
        original.call(item, stack, context, display, adder, flag);
        TooltipMutationHandler.mutate(TooltipPosition.ITEM, TooltipOrder.AFTER).accept(stack, context, adder, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.COMPONENT, TooltipOrder.BEFORE).accept(stack, context, adder, player, flag);
    }

    @WrapOperation(
            method = "addDetailsToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
                    ordinal = 21
            )
    )
    private void ApexCore$appendTooltipComponentAfter(
            ItemStack stack,
            DataComponentType<?> componentType,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag,
            Operation<Void> original,
            @Local(argsOnly = true) @Nullable Player player
    ) {
        original.call(stack, componentType, context, display, adder, flag);
        TooltipMutationHandler.mutate(TooltipPosition.COMPONENT, TooltipOrder.AFTER).accept(stack, context, adder, player, flag);
    }

    @Definition(id = "player", local = @Local(type = Player.class))
    @Expression("player != null")
    @ModifyExpressionValue(method = "addDetailsToTooltip", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean ApexCore$appendTooltipWarningBefore(
            boolean original,
            @Local(argsOnly = true) Consumer<Component> consumer,
            @Local(argsOnly = true)  Item.TooltipContext context,
            @Local(argsOnly = true) @Nullable Player player,
            @Local(argsOnly = true) TooltipFlag flag
    ) {
        TooltipMutationHandler.mutate(TooltipPosition.WARNING, TooltipOrder.BEFORE).accept((ItemStack) (Object) this, context, consumer, player, flag);
        return original;
    }

    @WrapOperation(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/EventHooks;onItemTooltip(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;Lnet/minecraft/world/item/Item$TooltipContext;)Lnet/neoforged/neoforge/event/entity/player/ItemTooltipEvent;"
            )
    )
    private ItemTooltipEvent ApexCore$appendTooltipFooterBefore(
            ItemStack stack,
            @Nullable Player player,
            List<Component> lines,
            TooltipFlag flag,
            Item.TooltipContext context,
            Operation<ItemTooltipEvent> original
    ) {
        TooltipMutationHandler.mutate(TooltipPosition.WARNING, TooltipOrder.AFTER).accept(stack, context, lines::add, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.FOOTER, TooltipOrder.BEFORE).accept(stack, context, lines::add, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.FOOTER, TooltipOrder.AFTER).accept(stack, context, lines::add, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.MODDED, TooltipOrder.BEFORE).accept(stack, context, lines::add, player, flag);
        var event = original.call(stack, player, lines, flag, context);
        TooltipMutationHandler.mutate(TooltipPosition.MODDED, TooltipOrder.AFTER).accept(stack, context, lines::add, player, flag);
        return event;
    }
}
