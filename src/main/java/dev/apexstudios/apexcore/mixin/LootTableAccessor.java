package dev.apexstudios.apexcore.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LootTable.class)
public interface LootTableAccessor {
    @Accessor("randomSequence")
    Optional<ResourceLocation> ApexCore$getRandomSequence();

    @Invoker("getRandomItems")
    ObjectArrayList<ItemStack> ApexCore$getRandomItems(LootContext context);

    @Invoker("shuffleAndSplitItems")
    void ApexCore$shuffleAndSplitItems(ObjectArrayList<ItemStack> stacks, int emptySlotCount, RandomSource random);

    @Accessor("LOGGER")
    static Logger ApexCore$getLogger() {
        throw new IllegalStateException("Mixin not injected!");
    }
}
