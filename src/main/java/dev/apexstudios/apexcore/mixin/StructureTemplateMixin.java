package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LootTableBlockEntityComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @WrapOperation(
            method = "placeInWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"
            )
    )
    private void ApexCore$loadWithComponents(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries, Operation<Void> original, @Local(argsOnly = true) RandomSource random) {
        original.call(blockEntity, tag, registries);
        LootTableBlockEntityComponent.setLootTableSeed(blockEntity, random);
    }
}
