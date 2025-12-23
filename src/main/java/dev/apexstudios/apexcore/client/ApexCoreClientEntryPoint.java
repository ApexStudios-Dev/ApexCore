package dev.apexstudios.apexcore.client;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.api.block.Dyeable;
import dev.apexstudios.apexcore.api.multiblock.MultiBlock;
import dev.apexstudios.apexcore.api.multiblock.SimpleHorizontalDirectionalMultiBlock;
import dev.apexstudios.apexcore.api.placement.BlockItemPlacementEvent;
import dev.apexstudios.apexcore.client.placement.PlacementVisualizerClient;
import dev.apexstudios.apexcore.common.ApexCore;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreClientEntryPoint {
    public ApexCoreClientEntryPoint(IEventBus modBus) {
        PlacementVisualizerClient.register(modBus);

        modBus.addListener(RegisterColorHandlersEvent.ItemTintSources.class, event -> event.register(ApexCore.identifier("dye_color"), DyeColorItemTintSource.MAP_CODEC));

        NeoForge.EVENT_BUS.addListener(ExtractLevelRenderStateEvent.class, event -> {
            var renderState = event.getRenderState();
            var level = event.getLevel();

            extractMultiBlockBreakingProgress(level, renderState);
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, BlockItemPlacementEvent.GetDefaultBlockState.class, event -> {
            if(event.blockState().getBlock() instanceof Dyeable dyeable) {
                event.setBlockState(dyeable.setDyedColor(event.blockState(), dyeable.getDyedColorForPlacement(event.placeContext())));
            }

            if(MultiBlock.isMultiBlock(event.blockState()) && event.blockState().hasProperty(SimpleHorizontalDirectionalMultiBlock.FACING)) {
                event.setBlockState(event
                        .blockState()
                        .setValue(SimpleHorizontalDirectionalMultiBlock.FACING, event
                                .placeContext()
                                .getHorizontalDirection()
                                .getOpposite()
                        )
                );
            }
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, BlockItemPlacementEvent.CollectAdditionalBlockStates.class, event -> {
            var blockState = event.blockState();

            if(MultiBlock.isMultiBlock(blockState)) {
                var index = MultiBlock.getIndex(blockState);

                MultiBlock.forEachPos(event.placeContext().getClickedPos(), blockState, (otherPos, otherBlockState) -> {
                    if(index != MultiBlock.getIndex(otherBlockState))
                        event.with(otherPos, otherBlockState);
                });
            }
        });
    }

    private void extractMultiBlockBreakingProgress(ClientLevel level, LevelRenderState renderState) {
        var additionalBreakingStates = Lists.<BlockBreakingRenderState>newArrayList();

        for(var breakingState : renderState.blockBreakingRenderStates) {
            var index = MultiBlock.getIndex(breakingState.blockState());

            MultiBlock.forEachPos(breakingState.blockPos(), breakingState.blockState(), (otherPos, otherBlockState) -> {
                if(MultiBlock.getIndex(otherBlockState) != index)
                    additionalBreakingStates.add(new BlockBreakingRenderState(otherPos, otherBlockState, breakingState.progress()));
            });
        }

        renderState.blockBreakingRenderStates.addAll(additionalBreakingStates);
    }
}
