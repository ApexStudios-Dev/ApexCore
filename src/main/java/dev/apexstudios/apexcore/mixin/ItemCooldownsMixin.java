package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.lib.util.CustomCooldownGroup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemCooldowns.class)
public class ItemCooldownsMixin {
    @ModifyReturnValue(
            method = "getCooldownGroup",
            at = @At("RETURN")
    )
    private Identifier ApexCore$getCooldownGroup(Identifier original, @Local(argsOnly = true) ItemStack stack) {
        if(stack.getItem() instanceof CustomCooldownGroup iface)
            return iface.computeCooldownGroup(stack);
        return original;
    }
}
