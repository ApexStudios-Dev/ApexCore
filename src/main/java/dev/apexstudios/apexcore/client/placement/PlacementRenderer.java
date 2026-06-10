package dev.apexstudios.apexcore.client.placement;

import dev.apexstudios.apexcore.api.multiblock.MultiBlock;
import dev.apexstudios.apexcore.api.placement.ExtractPlacementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public class PlacementRenderer {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlacementRenderer::extract);
        NeoForge.EVENT_BUS.addListener(PlacementRenderer::submit);

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ExtractPlacementRenderState.class, event -> {
            var blockState = event.blockState();
            var pos = event.placeContext.getClickedPos();

            if(blockState.hasProperty(BlockStateProperties.BED_PART)) {
                var part = blockState.getValue(BlockStateProperties.BED_PART);

                var otherPart = switch (part) {
                    case FOOT -> BedPart.HEAD;
                    case HEAD -> BedPart.FOOT;
                };

                event.put(
                        pos.relative(BedBlock.getConnectedDirection(blockState)),
                        blockState.setValue(BlockStateProperties.BED_PART, otherPart)
                );
            } else if(blockState.getBlock() instanceof MultiBlock) {
                MultiBlock.forEachPos(pos, blockState, event::put);
            }
        });
    }

    private static void extract(ExtractLevelRenderStateEvent event) {
        var client = Minecraft.getInstance();

        if(client.player == null) {
            return;
        }

        if(!(client.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }

        var level = event.getLevel();
        var placeContext = createPlacementContext(level, client.player, InteractionHand.MAIN_HAND, hitResult);

        if(placeContext == null) {
            placeContext = createPlacementContext(level, client.player, InteractionHand.OFF_HAND, hitResult);
        }

        if(placeContext != null) {
            var placeRenderState = new PlacementRenderState();
            placeRenderState.update(placeContext);
            event.getRenderState().setRenderData(PlacementRenderState.KEY, placeRenderState);
        }
    }

    private static @Nullable BlockPlaceContext createPlacementContext(Level level, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var stack = player.getItemInHand(hand);

        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) {
            return null;
        }

        if(item.getBlock() == Blocks.AIR) {
            return null;
        }

        return new BlockPlaceContext(level, player, hand, stack.copyWithCount(1), hitResult);
    }

    private static void submit(SubmitCustomGeometryEvent event) {
        var levelRenderState = event.getLevelRenderState();
        var placeRenderState = levelRenderState.getRenderData(PlacementRenderState.KEY);

        if(placeRenderState != null) {
            placeRenderState.submit(event.getPoseStack(), event.getSubmitNodeCollector(), levelRenderState.cameraRenderState);
        }
    }
}
