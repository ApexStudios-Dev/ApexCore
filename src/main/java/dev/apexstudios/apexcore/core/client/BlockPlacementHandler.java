package dev.apexstudios.apexcore.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.multiblock.MultiBlock;
import dev.apexstudios.apexcore.lib.multiblock.SimpleHorizontalDirectionalMultiBlock;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementRenderContext;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementRendererEvent;
import dev.apexstudios.apexcore.lib.util.ApexRenderTypes;
import dev.apexstudios.apexcore.lib.util.ApexTags;
import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public interface BlockPlacementHandler {
    ResourceLocation DBG_ALWAYS_RENDER_PLACEMENT = ApexCore.identifier("always_render_placement");
    ContextKey<RenderState> PLACEMENT_RENDER_KEY = new ContextKey<>(ApexCore.identifier("block_placement_state"));

    static void register(IEventBus modBus) {
        modBus.addListener(RegisterDebugEntriesEvent.class, event -> event.register(DBG_ALWAYS_RENDER_PLACEMENT, new DebugEntryNoop()));

        NeoForge.EVENT_BUS.addListener(BlockPlacementRendererEvent.UpdatePlacementContext.class, event -> {
            var renderContext = event.getRenderContext();
            var item = renderContext.item();
            var placeContext = event.getPlaceContext();

            if(item.value() instanceof BlockItem blockItem)
                event.setPlaceContext(blockItem.updatePlacementContext(placeContext));
        });

        NeoForge.EVENT_BUS.addListener(BlockPlacementRendererEvent.GetDefaultBlockState.class, BlockPlacementHandler::getBlockState);
        NeoForge.EVENT_BUS.addListener(BlockPlacementRendererEvent.GetPlacementBlockState.class, BlockPlacementHandler::getBlockState);

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, true, BlockPlacementRendererEvent.GetPlacementBlockState.class, event -> {
            if(!event.isCanceled())
                return;

            var placeContext = event.getPlaceContext();
            var blockState = event.getBlockState();

            // some blocks lose facing data when placement fails
            // this ensures these blocks get the corrected facing properties

            if(MultiBlock.isMultiBlock(blockState) && blockState.hasProperty(SimpleHorizontalDirectionalMultiBlock.FACING))
                event.setBlockState(blockState.setValue(SimpleHorizontalDirectionalMultiBlock.FACING, placeContext.getHorizontalDirection().getOpposite()));
            else if(blockState.getBlock() instanceof BedBlock && blockState.hasProperty(BedBlock.FACING))
                event.setBlockState(blockState.setValue(BedBlock.FACING, placeContext.getHorizontalDirection()));
        });

        NeoForge.EVENT_BUS.addListener(BlockPlacementRendererEvent.StripInvalidProperties.class, event -> event.strip(BlockStateProperties.WATERLOGGED));

        NeoForge.EVENT_BUS.addListener(BlockPlacementRendererEvent.CollectAdditionalBlockStates.class, event -> {
            var renderContext = event.getRenderContext();
            var blockState = event.getBlockState();

            if(blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                var half = blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
                var otherHalf = half.getOtherHalf();
                var otherOffset = half.getDirectionToOther();
                var otherPos = renderContext.pos().relative(otherOffset);
                event.with(otherPos, blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, otherHalf));
            }

            if(blockState.hasProperty(BlockStateProperties.BED_PART)) {
                var part = blockState.getValue(BlockStateProperties.BED_PART);

                var otherPart = switch(part) {
                    case FOOT -> BedPart.HEAD;
                    case HEAD -> BedPart.FOOT;
                };

                var otherOffset = BedBlock.getConnectedDirection(blockState);
                var otherPos = renderContext.pos().relative(otherOffset);
                event.with(otherPos, blockState.setValue(BlockStateProperties.BED_PART, otherPart));
            }

            if(MultiBlock.isMultiBlock(blockState)) {
                var index = MultiBlock.getIndex(blockState);

                MultiBlock.forEachPos(renderContext.pos(), blockState, (otherPos, otherBlockState) -> {
                    if(index != MultiBlock.getIndex(otherBlockState))
                        event.with(otherPos, otherBlockState);
                });
            }
        });
    }

    static void extractBlockPlacement(ClientLevel level, LevelRenderState renderState) {
        var client = Minecraft.getInstance();
        var player = client.player;

        if(player == null || !(client.hitResult instanceof BlockHitResult hitResult))
            return;

        var forceRender = client.debugEntries.isCurrentlyEnabled(DBG_ALWAYS_RENDER_PLACEMENT);

        if(hitResult.getType() == HitResult.Type.MISS && !forceRender)
            return;

        var mainArm = player.getMainArm();
        var placementRenderState = extractBlockPlacementForArm(level, hitResult, player, mainArm);

        if(placementRenderState == null)
            placementRenderState = extractBlockPlacementForArm(level, hitResult, player, mainArm.getOpposite());
        if(placementRenderState != null)
            renderState.setRenderData(PLACEMENT_RENDER_KEY, placementRenderState);
    }

    @Nullable
    private static RenderState extractBlockPlacementForArm(ClientLevel level, BlockHitResult hitResult, LocalPlayer player, HumanoidArm arm) {
        var renderContext = new BlockPlacementRenderContext(level, hitResult, player, arm);

        if(renderContext.item().isEmpty())
            return null;

        var enabledFeatures = level.enabledFeatures();
        // placement should fail if item is disabled
        var canPlace = hitResult.getType() != HitResult.Type.MISS && renderContext.item().value().isEnabled(enabledFeatures);
        // 1) determine block for placement
        var block = NeoForge.EVENT_BUS.post(new BlockPlacementRendererEvent.GetBlock(renderContext)).getBlock();

        // got empty block, move onto next arm
        if(block == Blocks.AIR)
            return null;
        // block does not have required tag, move onto next arm
        if(!block.builtInRegistryHolder().is(ApexTags.Blocks.RENDER_PLACEMENT_WHITELIST))
            return null;
        // placement should fail if block is disabled
        if(canPlace && !block.isEnabled(enabledFeatures))
            canPlace = false;

        // 2) calculate placement context
        var eventUPC = NeoForge.EVENT_BUS.post(new BlockPlacementRendererEvent.UpdatePlacementContext(renderContext));
        var placeContext = eventUPC.getPlaceContext();
        // redirect render context to new placement position
        renderContext = renderContext.at(placeContext.getLevel(), placeContext.getClickedPos(), placeContext.getClickedFace());

        // placement failed if invalid context or event was cancelled
        // similar result to returning null in 'BlockItem.updatePlacementContext'
        if(canPlace && (!placeContext.canPlace() || eventUPC.isCanceled()))
            canPlace = false;

        // 3) determine default block state
        var defaultBlockState = NeoForge.EVENT_BUS.post(new BlockPlacementRendererEvent.GetDefaultBlockState(renderContext, placeContext, block)).getBlockState();

        // 4) determine placement block state
        var eventGPBS = NeoForge.EVENT_BUS.post(new BlockPlacementRendererEvent.GetPlacementBlockState(renderContext, placeContext, defaultBlockState));
        var placementBlockState = eventGPBS.getBlockState();

        // canceling this event means block placement failed
        // similar result to returning null in 'Block.getStateForPlacement'
        if(canPlace && eventGPBS.isCanceled())
            canPlace = false;

        // 5) strip invalid block states
        // by default this strips out waterlogged
        placementBlockState = NeoForge.EVENT_BUS.post(new BlockPlacementRendererEvent.StripInvalidProperties(defaultBlockState, placementBlockState)).getPlacementBlockState();

        // 6) collect additional block states
        var blockStates = new Long2ObjectOpenHashMap<BlockState>();
        NeoForge.EVENT_BUS.post(new BlockPlacementRendererEvent.CollectAdditionalBlockStates(renderContext, placeContext, placementBlockState, blockStates));
        blockStates.put(renderContext.pos().asLong(), placementBlockState); // ensure origin point can not be overwritten

        // 7) return finalized render state
        return new RenderState(
                renderContext.level(),
                canPlace,
                Long2ObjectMaps.unmodifiable(blockStates)
        );
    }

    static void submitBlockPlacement(PoseStack pose, LevelRenderState renderState, SubmitNodeCollector collector) {
        var placementState = renderState.getRenderData(PLACEMENT_RENDER_KEY);

        if(placementState == null)
            return;

        pose.pushPose();
        pose.translate(-renderState.cameraRenderState.pos.x, -renderState.cameraRenderState.pos.y, -renderState.cameraRenderState.pos.z);

        collector.submitCustomGeometry(pose, ApexRenderTypes.entityTranslucentNoDepth(TextureAtlas.LOCATION_BLOCKS), (p, consumer) -> {
            var renderPose = new PoseStack();
            renderPose.mulPose(p.pose());
            renderBlockPlacement(renderPose, consumer, placementState);
        });

        pose.popPose();
    }

    private static void renderBlockPlacement(PoseStack pose, VertexConsumer consumer, RenderState renderState) {
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        var ghostConsumer = new GhostVertexConsumer(consumer, 170);

        for(var entry : renderState.blockStates.long2ObjectEntrySet()) {
            var pos = BlockPos.of(entry.getLongKey());
            var blockState = entry.getValue();

            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());

            // Copy of MovingBlockRenderState render logic in BlockFeatureRenderer
            var modelParts = blockRenderDispatcher.getBlockModel(blockState).collectParts(
                    renderState.level,
                    pos,
                    blockState,
                    RandomSource.create(blockState.getSeed(pos))
            );

            blockRenderDispatcher.getModelRenderer().tesselateBlock(
                    renderState.level,
                    modelParts,
                    blockState,
                    pos,
                    pose,
                    renderType -> ghostConsumer,
                    false,
                    renderState.canBePlaced ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(OverlayTexture.NO_WHITE_U, OverlayTexture.RED_OVERLAY_V)
            );

            pose.popPose();
        }
    }

    private static void getBlockState(BlockPlacementRendererEvent.GetBlockState event) {
        var renderContext = event.getRenderContext();
        var item = renderContext.item();
        var placeContext = event.getPlaceContext();

        if(item.getItem() instanceof BlockItem blockItem) {
            var blockState = ((BlockItemAccessor) blockItem).ApexCore$getPlacementState(placeContext);
            var forPlacement = event instanceof BlockPlacementRendererEvent.GetPlacementBlockState;

            // null here means 'BlockItem.canPlace' returned false
            // 'GetDefaultBlockState' requires non-null
            // 'GetPlacementBlockState' cancels event on null block states (cancelled means placement failed)
            if(blockState != null || forPlacement)
                event.setBlockState(blockState);
        }
    }

    record RenderState(
            BlockAndTintGetter level,
            boolean canBePlaced,
            Long2ObjectMap<BlockState> blockStates
    ) {}
}
