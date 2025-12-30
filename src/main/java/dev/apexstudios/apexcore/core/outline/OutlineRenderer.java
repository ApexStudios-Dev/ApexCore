package dev.apexstudios.apexcore.core.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.apexcore.lib.multiblock.MultiBlock;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class OutlineRenderer {
    private static final Map<BlockState, List<Outline>> cachedOutlines = new Reference2ObjectOpenHashMap<>();

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ExtractBlockOutlineRenderStateEvent.class, event -> {
            /*if(true) {
                return;
            }*/
            var blockState = event.getBlockState();
            cachedOutlines.computeIfAbsent(blockState, state -> extract(event.getLevel(), event.getBlockPos(), state));
            event.addCustomRenderer((renderState, bufferSource, poseStack, translucentPass, levelRenderState) -> renderOutline(blockState, renderState, bufferSource, poseStack, translucentPass, levelRenderState));
        });

        modBus.addListener(TextureAtlasStitchedEvent.class, event -> cachedOutlines.clear());
    }

    private static List<Outline> extract(ClientLevel level, BlockPos pos, BlockState blockState) {
        var random = level.getRandom();
        var outlines = Outline.extract(level, pos, blockState, random);

        /*if(MultiBlock.isMultiBlock(blockState)) {
            MultiBlock.forEachPos(pos, blockState, (otherPos, otherBlockState) -> cachedOutlines.put(otherBlockState, Outline.extract(level, otherPos, otherBlockState, random)));
        }*/

        return outlines;
    }

    private static boolean renderOutline(BlockState blockState, BlockOutlineRenderState renderState, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
        poseStack.pushPose();

        var cameraPos = levelRenderState.cameraRenderState.pos;
        var highContrast = renderState.highContrast();
        var translucent = renderState.isTranslucent() || translucentPass;

        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        if(MultiBlock.isMultiBlock(blockState)) {
            poseStack.pushPose();

            MultiBlock.forEachPos(renderState.pos(), blockState, (otherPos, otherBlockState) -> {
                renderOutline(otherBlockState, otherPos, bufferSource, poseStack, highContrast, translucent);
            });

            poseStack.popPose();
        } else {
            renderOutline(blockState, renderState.pos(), bufferSource, poseStack, highContrast, translucent);
        }

        poseStack.popPose();
        return true;
    }

    private static void renderOutline(BlockState blockState, BlockPos pos, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean highContrast, boolean translucent) {
        var outlines = cachedOutlines.get(blockState);
        // var outlines = extract(Minecraft.getInstance().level, pos, blockState);

        if(outlines == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        renderOutline(outlines, bufferSource, poseStack, highContrast, translucent);

        poseStack.popPose();
    }

    private static void renderOutline(List<Outline> outlines, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean highContrast, boolean translucent) {
        if(highContrast) {
            var buffer = bufferSource.getBuffer(RenderTypes.secondaryBlockOutline());
            renderOutline(outlines, buffer, poseStack, CommonColors.BLACK, 7F);
        }

        var buffer = bufferSource.getBuffer(translucent ? RenderTypes.linesTranslucent() :RenderTypes.lines());
        var color = highContrast ? CommonColors.HIGH_CONTRAST_DIAMOND : ARGB.black(102);
        var width = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        renderOutline(outlines, buffer, poseStack, color, width);

        bufferSource.endLastBatch();
    }

    private static void renderOutline(Collection<Outline> outlines, VertexConsumer buffer, PoseStack poseStack, int color, float width) {
        var pos = new Vector4f();
        var nml = new Vector3f();
        var last = poseStack.last();
        var pose = last.pose();
        var poseNormal = last.normal();

        for(var outline : outlines) {
            poseNormal.transform(outline.nX(), outline.nY(), outline.nZ(), nml);

            pose.transform(outline.x1(), outline.y1(), outline.z1(), 1F, pos);
            buffer.addVertex(pos.x(), pos.y(), pos.z())
                    .setLineWidth(width)
                    .setColor(color)
                    .setNormal(nml.x(), nml.y(), nml.z());

            pose.transform(outline.x2(), outline.y2(), outline.z2(), 1F, pos);
            buffer.addVertex(pos.x(), pos.y(), pos.z())
                    .setLineWidth(width)
                    .setColor(color)
                    .setNormal(nml.x(), nml.y(), nml.z());
        }
    }
}
