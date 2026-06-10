package dev.apexstudios.apexcore.mixin.client;

import java.util.List;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModelRenderState.class)
public interface BlockModelRenderStateAccessor {
    @Accessor("hasTranslucency")
    boolean ApexCore$hasTranslucency();

    @Accessor("modelParts")
    @Nullable
    List<BlockStateModelPart> ApexCore$getModelParts();
}
