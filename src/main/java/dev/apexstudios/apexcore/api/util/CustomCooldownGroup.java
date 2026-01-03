package dev.apexstudios.apexcore.api.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface CustomCooldownGroup {
    Identifier computeCooldownGroup(ItemStack stack);
}
