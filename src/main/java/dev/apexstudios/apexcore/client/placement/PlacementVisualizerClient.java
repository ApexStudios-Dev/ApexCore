package dev.apexstudios.apexcore.client.placement;

import dev.apexstudios.apexcore.api.placement.BlockItemPlacementEvent;
import dev.apexstudios.apexcore.api.placement.PlacementPreviewHandler;
import dev.apexstudios.apexcore.api.placement.PlacementRenderTypes;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public interface PlacementVisualizerClient {
    static void register(IEventBus modBus) {
        addRequiredListeners(modBus);
        addBlockItemListeners();
    }

    private static void addRequiredListeners(IEventBus modBus) {
        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(() -> PlacementPreviewHandler.register(ApexCore.identifier("block_item"), new BlockItemPreviewHandler())));
        modBus.addListener(RegisterRenderPipelinesEvent.class, event -> event.registerPipeline(PlacementRenderTypes.Pipelines.TRANSLUCENT_NO_DEPTH));
        modBus.addListener(RegisterDebugEntriesEvent.class, event -> event.register(PlacementPreviewRegistry.DEBUG_KEY, new DebugEntryNoop()));

        NeoForge.EVENT_BUS.addListener(ExtractLevelRenderStateEvent.class, PlacementPreviewRegistry::extract);

        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterSky.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterOpaqueBlocks.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterEntities.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterTranslucentBlocks.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterTripwireBlocks.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterParticles.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterWeather.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterLevel.class, PlacementPreviewRegistry::submit);
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
}
