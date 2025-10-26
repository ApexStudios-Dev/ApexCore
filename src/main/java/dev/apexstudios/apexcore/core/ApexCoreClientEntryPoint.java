package dev.apexstudios.apexcore.core;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.core.client.DyeColorItemTintSource;
import dev.apexstudios.apexcore.lib.block.Dyeable;
import dev.apexstudios.apexcore.lib.multiblock.MultiBlock;
import dev.apexstudios.apexcore.lib.multiblock.SimpleHorizontalDirectionalMultiBlock;
import dev.apexstudios.placementvisualizer.api.BlockItemPlacementEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
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
        modBus.addListener(RegisterColorHandlersEvent.ItemTintSources.class, event -> event.register(ApexCore.identifier("dye_color"), DyeColorItemTintSource.MAP_CODEC));

        NeoForge.EVENT_BUS.addListener(ExtractLevelRenderStateEvent.class, event -> {
            var renderState = event.getRenderState();
            var level = event.getLevel();

            extractMultiBlockBreakingProgress(level, renderState);
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, true, BlockItemPlacementEvent.GetPlacementBlockState.class, event -> {
            if(!event.isCanceled()) {
                return;
            }

            if(event.blockState().hasProperty(Dyeable.PROPERTY)) {
                event.setBlockState(event.blockState().setValue(Dyeable.PROPERTY, Dyeable.getColorForPlacement(event.placeContext())));
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

        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.CollectAdditionalBlockStates.class, event -> {
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
            var index = MultiBlock.getIndex(breakingState.blockState);

            MultiBlock.forEachPos(breakingState.blockPos, breakingState.blockState, (otherPos, otherBlockState) -> {
                if(MultiBlock.getIndex(otherBlockState) != index)
                    additionalBreakingStates.add(new BlockBreakingRenderState(level, otherPos, breakingState.progress));
            });
        }

        renderState.blockBreakingRenderStates.addAll(additionalBreakingStates);
    }
}
