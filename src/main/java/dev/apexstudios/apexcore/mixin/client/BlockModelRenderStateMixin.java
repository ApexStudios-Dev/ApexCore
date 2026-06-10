package dev.apexstudios.apexcore.mixin.client;

import dev.apexstudios.apexcore.client.placement.PlacementRenderState;
import java.util.List;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Find a better way to render with translucency
@Mixin(BlockModelRenderState.class)
public abstract class BlockModelRenderStateMixin {
    @Shadow
    private boolean hasTranslucency;

    @Shadow
    @Nullable
    private RenderType renderType;

    @Inject(
            method = "setupModel",
            at = @At("TAIL")
    )
    private void ApexCore$setupModel(Matrix4fc transformation, boolean hasTranslucency, CallbackInfoReturnable<List<BlockStateModelPart>> cir) {
        if(PlacementRenderState.EXTRACTING_PLACEMENT) {
            this.hasTranslucency = true;
            renderType = RenderTypes.translucentMovingBlock();
        }
    }
}
