package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.core.util.TooltipMutationHandler;
import dev.apexstudios.apexcore.lib.tooltip.TooltipOrder;
import dev.apexstudios.apexcore.lib.tooltip.TooltipPosition;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void ApexCore$appendTooltipHeaderBefore(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> consumer) {
        TooltipMutationHandler.mutate(TooltipPosition.HEADER, TooltipOrder.BEFORE).accept((ItemStack) (Object) this, context, consumer::add, player, flag);
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void ApexCore$appendTooltipHeaderAfter(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> consumer) {
        TooltipMutationHandler.mutate(TooltipPosition.HEADER, TooltipOrder.AFTER).accept((ItemStack) (Object) this, context, consumer::add, player, flag);
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z",
                    ordinal = 2,
                    shift = At.Shift.BEFORE
            )
    )
    private void ApexCore$appendTooltipItemBefore(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local Consumer<Component> consumer) {
        TooltipMutationHandler.mutate(TooltipPosition.ITEM, TooltipOrder.BEFORE).accept((ItemStack) (Object) this, context, consumer, player, flag);
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void ApexCore$appendTooltipComponentBefore(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local Consumer<Component> consumer) {
        var self = (ItemStack) (Object) this;
        TooltipMutationHandler.mutate(TooltipPosition.ITEM, TooltipOrder.AFTER).accept(self, context, consumer, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.COMPONENT, TooltipOrder.BEFORE).accept(self, context, consumer, player, flag);
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
                    ordinal = 8,
                    shift = At.Shift.AFTER
            )
    )
    private void ApexCore$appendTooltipComponentAfter(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local Consumer<Component> consumer) {
        TooltipMutationHandler.mutate(TooltipPosition.COMPONENT, TooltipOrder.AFTER).accept((ItemStack) (Object) this, context, consumer, player, flag);
    }

    @Definition(id = "player", local = @Local(type = Player.class))
    @Expression("player != null")
    @ModifyExpressionValue(method = "getTooltipLines", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean ApexCore$appendTooltipWarningBefore(
            boolean original,
            @Local Consumer<Component> consumer,
            @Local(argsOnly = true)  Item.TooltipContext context,
            @Local(argsOnly = true) @Nullable Player player,
            @Local(argsOnly = true) TooltipFlag flag
    ) {
        TooltipMutationHandler.mutate(TooltipPosition.WARNING, TooltipOrder.BEFORE).accept((ItemStack) (Object) this, context, consumer, player, flag);
        return original;
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/EventHooks;onItemTooltip(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;Lnet/minecraft/world/item/Item$TooltipContext;)Lnet/neoforged/neoforge/event/entity/player/ItemTooltipEvent;",
                    shift = At.Shift.BEFORE
            )
    )
    private void ApexCore$appendTooltipFooterBefore(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local Consumer<Component> consumer) {
        var self = (ItemStack) (Object) this;
        TooltipMutationHandler.mutate(TooltipPosition.WARNING, TooltipOrder.AFTER).accept(self, context, consumer, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.FOOTER, TooltipOrder.BEFORE).accept(self, context, consumer, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.FOOTER, TooltipOrder.AFTER).accept(self, context, consumer, player, flag);
        TooltipMutationHandler.mutate(TooltipPosition.MODDED, TooltipOrder.BEFORE).accept(self, context, consumer, player, flag);
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/event/EventHooks;onItemTooltip(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;Lnet/minecraft/world/item/Item$TooltipContext;)Lnet/neoforged/neoforge/event/entity/player/ItemTooltipEvent;",
                    shift = At.Shift.AFTER
            )
    )
    private void ApexCore$appendTooltipFooterAfter(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local Consumer<Component> consumer) {
        TooltipMutationHandler.mutate(TooltipPosition.MODDED, TooltipOrder.AFTER).accept((ItemStack) (Object) this, context, consumer, player, flag);
    }
}
