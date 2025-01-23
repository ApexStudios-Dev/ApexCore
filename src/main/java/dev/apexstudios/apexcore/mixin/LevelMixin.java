package dev.apexstudios.apexcore.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.apexstudios.apexcore.lib.component.block.types.MultiBlockComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Nullable
    @WrapMethod(method = "getBlockEntity")
    private BlockEntity getBlockEntity(BlockPos pos, Operation<BlockEntity> original) {
        var self = Level.class.cast(this);
        var blockEntityPos = MultiBlockComponent.getBlockEntityPos(self, pos);
        return original.call(blockEntityPos);
    }
}
