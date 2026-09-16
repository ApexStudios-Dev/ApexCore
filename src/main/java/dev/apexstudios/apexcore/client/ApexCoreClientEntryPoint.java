package dev.apexstudios.apexcore.client;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.api.multiblock.MultiBlock;
import dev.apexstudios.apexcore.common.ApexCore;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.api.distmarker.Dist;
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
