package dev.apexstudios.apexcore.client.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.api.placement.BlockItemPlacementEvent;
import dev.apexstudios.apexcore.api.placement.PlacementRenderTypes;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import java.util.List;

public interface PlacementVisualizerClient {
    Identifier DEBUG_KEY = ApexCore.identifier("placement_renderer/force_render");
    ContextKey<State> KEY = new ContextKey<>(ApexCore.identifier("placement_renderer"));

    static void register(IEventBus modBus) {
        addRequiredListeners(modBus);
        addBlockItemListeners();
    }

    private static void addRequiredListeners(IEventBus modBus) {
        modBus.addListener(RegisterRenderPipelinesEvent.class, event -> event.registerPipeline(PlacementRenderTypes.Pipelines.TRANSLUCENT_NO_DEPTH));
        modBus.addListener(RegisterDebugEntriesEvent.class, event -> event.register(DEBUG_KEY, new DebugEntryNoop()));

        NeoForge.EVENT_BUS.addListener(ExtractLevelRenderStateEvent.class, PlacementVisualizerClient::extract);
        NeoForge.EVENT_BUS.addListener(SubmitCustomGeometryEvent.class, event -> submit(event.getLevelRenderState(), event.getPoseStack(), event.getSubmitNodeCollector()));
    }

    private static void addBlockItemListeners() {
        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.UpdatePlacementContext.class, event -> {
            var placeContext = event.placeContext();

            if(placeContext.getItemInHand().getItem() instanceof BlockItem blockItem) {
                event.setPlaceContext(blockItem.updatePlacementContext(placeContext));
            }
        });

        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.GetDefaultBlockState.class, PlacementVisualizerClient::getBlockState);
        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.GetPlacementBlockState.class, PlacementVisualizerClient::getBlockState);

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, true, BlockItemPlacementEvent.GetPlacementBlockState.class, event -> {
            if(!event.isCanceled())
                return;

            // some blocks lose facing data when placement fails
            // this ensures these blocks get the corrected facing properties

            if(event.blockState().getBlock() instanceof BedBlock && event.blockState().hasProperty(BedBlock.FACING))
                event.setBlockState(event.blockState().setValue(BedBlock.FACING, event.placeContext().getHorizontalDirection()));
        });

        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.StripInvalidProperties.class, event -> event.strip(BlockStateProperties.WATERLOGGED));

        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.CollectAdditionalBlockStates.class, event -> {
            var blockState = event.blockState();

            if(blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                var half = blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
                var otherHalf = half.getOtherHalf();
                var otherOffset = half.getDirectionToOther();
                var otherPos = event.placeContext().getClickedPos().relative(otherOffset);
                event.with(otherPos, blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, otherHalf));
            }

            if(blockState.hasProperty(BlockStateProperties.BED_PART)) {
                var part = blockState.getValue(BlockStateProperties.BED_PART);

                var otherPart = switch(part) {
                    case FOOT -> BedPart.HEAD;
                    case HEAD -> BedPart.FOOT;
                };

                var otherOffset = BedBlock.getConnectedDirection(blockState);
                var otherPos = event.placeContext().getClickedPos().relative(otherOffset);
                event.with(otherPos, blockState.setValue(BlockStateProperties.BED_PART, otherPart));
            }
        });
    }

    private static void getBlockState(BlockItemPlacementEvent.GetBlockState event) {
        var placeContext = event.placeContext();

        if(placeContext.getItemInHand().getItem() instanceof BlockItem blockItem) {
            var blockState = ((BlockItemAccessor) blockItem).PlacementVisualizer$getPlacementState(placeContext);
            var forPlacement = event instanceof BlockItemPlacementEvent.GetPlacementBlockState;

            // null here means 'BlockItem.canPlace' returned false
            // 'GetDefaultBlockState' requires non-null
            // 'GetPlacementBlockState' cancels event on null block states (cancelled means placement failed)
            if(blockState != null || forPlacement)
                event.setBlockState(blockState);
        }
    }

    private static void extract(ExtractLevelRenderStateEvent event) {
        var client = Minecraft.getInstance();

        if(client.player == null || !(client.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }

        var forceRender = Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DEBUG_KEY);

        if(hitResult.getType() == HitResult.Type.MISS && !forceRender) {
            return;
        }

        var level = event.getLevel();
        var levelState = event.getRenderState();

        if(!extract(levelState, level, hitResult, client.player, InteractionHand.MAIN_HAND)) {
            extract(levelState, level, hitResult, client.player, InteractionHand.OFF_HAND);
        }
    }

    private static boolean extract(LevelRenderState levelState, ClientLevel level, BlockHitResult hitResult, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if(stack.isEmpty()) {
            return false;
        }

        var enabledFeatures = level.enabledFeatures();
        // placement should fail if item is disabled
        var canPlace = hitResult.getType() != HitResult.Type.MISS && stack.isItemEnabled(enabledFeatures);
        // 1) determine block for placement
        var block = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetBlock(stack)).block();

        // got empty block, move onto next arm
        if(block == Blocks.AIR) {
            return false;
        }

        // block does not have required tag, move onto next arm
        if(!block.builtInRegistryHolder().is(BlockItemPlacementEvent.RENDERABLES)) {
            return false;
        }

        // placement should fail if block is disabled
        if(canPlace && !block.isEnabled(enabledFeatures)) {
            canPlace = false;
        }

        // 2) calculate placement context
        var eventUPC = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.UpdatePlacementContext(level, player, hand, stack, hitResult));
        var placeContext = eventUPC.placeContext();
        var placementPos = placeContext.getClickedPos();

        // placement failed if invalid context or event was cancelled
        // similar result to returning null in 'BlockItem.updatePlacementContext'
        if(canPlace && (!placeContext.canPlace() || eventUPC.isCanceled())) {
            canPlace = false;
        }

        // mark as invalid placement if out of bounds
        // 'level.isInWorldBounds' only checks the max/min world bounds
        // but we are after the world border bounds
        if(canPlace && !level.getWorldBorder().isWithinBounds(placementPos)) {
            canPlace = false;
        }

        // make as invalid placement if gamemode or data components dictate so
        // mainly for adventure mode + 'can_place_on' data component
        if(canPlace && !player.mayUseItemAt(placementPos, hitResult.getDirection(), stack)) {
            canPlace = false;
        }

        // 3) determine default block state
        var defaultBlockState = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetDefaultBlockState(placeContext, block)).blockState();

        // 4) load block state data from data components
        // copied from 'BlockItem#updateBlockStateFromTag'
        // since vanilla tries to 'setBlock' if state changed
        var blockStateProperties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);

        // load block properties onto default block state
        if(!blockStateProperties.isEmpty()) {
            defaultBlockState = blockStateProperties.apply(defaultBlockState);
        }

        // 5) determine placement block state
        var eventGPBS = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetPlacementBlockState(placeContext, defaultBlockState));
        var placementBlockState = eventGPBS.blockState();

        // load block properties onto placement block state
        if(!blockStateProperties.isEmpty()) {
            placementBlockState = blockStateProperties.apply(placementBlockState);
        }

        // canceling this event means block placement failed
        // similar result to returning null in 'Block.getStateForPlacement'
        if(canPlace && eventGPBS.isCanceled()) {
            canPlace = false;
        }

        // 6) strip invalid block states
        // by default this strips out waterlogged
        placementBlockState = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.StripInvalidProperties(defaultBlockState, placementBlockState)).placementBlockState();

        // 7) collect additional block states
        var blockStates = new Long2ObjectOpenHashMap<BlockState>();
        NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.CollectAdditionalBlockStates(placeContext, placementBlockState, blockStates));
        blockStates.put(placementPos.asLong(), placementBlockState); // ensure origin point can not be overwritten

        // 8) extract block render states
        var renderStates = new Long2ObjectOpenHashMap<MovingBlockRenderState>(blockStates.size());

        blockStates.forEach((id, blockState) -> {
            var pos = BlockPos.of(id);
            var movingBlock = new MovingBlockRenderState();
            movingBlock.biome = level.getBiome(pos);
            movingBlock.blockPos = pos;
            movingBlock.randomSeedPos = pos;
            movingBlock.blockState = blockState;
            movingBlock.lightEngine = level.getLightEngine();
            movingBlock.cardinalLighting = level.cardinalLighting();
            movingBlock.modelData = level.getModelData(pos);

            renderStates.put(id, movingBlock);
        });

        // 9) Extract block entity render states
        var blockEntityRenderStates = BlockEntityPreviewHandler.extractAll(levelState, level, 0F, stack, blockStates);

        // 10) return finalized render state
        levelState.setRenderData(KEY, new State(
                canPlace,
                Long2ObjectMaps.unmodifiable(renderStates),
                blockEntityRenderStates
        ));

        return true;
    }

    private static void submit(LevelRenderState levelRenderState, PoseStack poseStack, SubmitNodeCollector nodes) {
        var state = levelRenderState.getRenderData(KEY);

        if(state == null) {
            return;
        }

        var collector = new GhostNodeStorage(nodes, state.canPlace);

        poseStack.pushPose();
        poseStack.translate(levelRenderState.cameraRenderState.pos.scale(-1D));

        state.submit(levelRenderState, poseStack, collector);

        poseStack.popPose();
    }

    record State(boolean canPlace, Long2ObjectMap<MovingBlockRenderState> blockStates, List<BlockEntityRenderState> blockEntityRenderStates) {
        public void submit(LevelRenderState levelRenderState, PoseStack poseStack, SubmitNodeCollector collector) {
            for(var entry : blockStates.long2ObjectEntrySet()) {
                var pos = BlockPos.of(entry.getLongKey());
                var blockState = entry.getValue();

                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                collector.submitMovingBlock(
                        poseStack,
                        blockState,
                        0
                );

                poseStack.popPose();
            }

            BlockEntityPreviewHandler.submitAll(poseStack, collector, levelRenderState, blockEntityRenderStates);
        }
    }
}
