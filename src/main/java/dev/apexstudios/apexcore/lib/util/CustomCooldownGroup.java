package dev.apexstudios.apexcore.lib.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface CustomCooldownGroup {
    ResourceLocation computeCooldownGroup(ItemStack stack);
}
