package dev.apexstudios.apexcore.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import dev.apexstudios.apexcore.api.block.Dyeable;
import dev.apexstudios.apexcore.api.multiblock.MultiBlock;
import dev.apexstudios.apexcore.api.multiblock.SimpleHorizontalDirectionalMultiBlock;
import dev.apexstudios.apexcore.api.placement.BlockItemPlacementEvent;
import dev.apexstudios.apexcore.client.placement.PlacementVisualizerClient;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.common.network.ServerboundSetModifierKeyPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreClientEntryPoint {
    public static final KeyMapping MODIFIER = new KeyMapping("key." + ApexCore.ID + ".modifier", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_LCONTROL, KeyMapping.Category.GAMEPLAY);

    public ApexCoreClientEntryPoint(IEventBus modBus) {
        PlacementVisualizerClient.register(modBus);

        modBus.addListener(RegisterColorHandlersEvent.ItemTintSources.class, event -> event.register(ApexCore.identifier("dye_color"), DyeColorItemTintSource.MAP_CODEC));
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(MODIFIER));

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

        NeoForge.EVENT_BUS.addListener(InputEvent.Key.class, event -> {
            var player = Minecraft.getInstance().player;
            var action = event.getAction();

            if(player != null && MODIFIER.matches(event.getKeyEvent()) && action != InputConstants.REPEAT) {
                var state = action == InputConstants.PRESS;
                player.setData(ApexCore.PLAYER_MODIFIER, state);
                player.connection.send(new ServerboundSetModifierKeyPacket(state));
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
