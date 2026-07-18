package dev.apexstudios.apexcore.client.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.api.ghost.GhostBedPartPlacementExtractor;
import dev.apexstudios.apexcore.api.ghost.GhostBlockState;
import dev.apexstudios.apexcore.api.ghost.GhostLevel;
import dev.apexstudios.apexcore.api.ghost.GhostPlacementExtractor;
import dev.apexstudios.apexcore.api.ghost.GhostUtil;
import dev.apexstudios.apexcore.api.ghost.RegisterGhostPlacementExtractorsEvent;
import dev.apexstudios.apexcore.common.ApexCore;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.InitializeClientRegistriesEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class GhostPlacementHandler {
    private static final ContextKey<GhostLevel> ALYWAYS_ON_TOP_KEY = new ContextKey<>(ApexCore.identifier("placement_level/always_on_top"));
    private static final ContextKey<GhostLevel> AFTER_TERRAIN_KEY = new ContextKey<>(ApexCore.identifier("placement_level/after_terrain"));
    private static final Identifier DEBUG = ApexCore.identifier("always_render_placement");
    private static final Map<Item, GhostPlacementExtractor> EXTRACTOR_MAP = new IdentityHashMap<>();
    private static final GhostPlacementExtractor DEFAULT_EXTRACTOR = new GhostPlacementExtractor() { };

    public static void register() {
        ApexCore.REGISTREE.event(RegisterDebugEntriesEvent.class, event -> event.register(DEBUG, new DebugEntryNoop()));
        ApexCore.REGISTREE.event(EventPriority.HIGH, GhostPlacementHandler::registerBuiltIn);
        ApexCore.REGISTREE.event(InitializeClientRegistriesEvent.class, event -> ModLoader.postEvent(new RegisterGhostPlacementExtractorsEvent(EXTRACTOR_MAP)));

        NeoForge.EVENT_BUS.addListener(ExtractLevelRenderStateEvent.class, event -> extract(event.getLevel(), event.getRenderState()));

        NeoForge.EVENT_BUS.addListener(SubmitCustomGeometryEvent.class, event -> {
            var levelRenderState = event.getLevelRenderState();
            var nodeCollector = event.getSubmitNodeCollector();
            var poseStack = event.getPoseStack();

            submit(nodeCollector, poseStack, levelRenderState, ALYWAYS_ON_TOP_KEY);
            submit(nodeCollector, poseStack, levelRenderState, AFTER_TERRAIN_KEY);
        });
    }

    @SuppressWarnings("deprecation")
    private static void registerBuiltIn(RegisterGhostPlacementExtractorsEvent event) {
        for(var block : BuiltInRegistries.BLOCK) {
            if(!block.builtInRegistryHolder().key().identifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
                continue;
            }

            if(block instanceof BedBlock) {
                event.registerBlock(block, new GhostBedPartPlacementExtractor() {});
            }
        }
    }

    private static void extract(ClientLevel level, LevelRenderState levelRenderState) {
        var client = Minecraft.getInstance();
        var player = client.player;
        var alwaysRender = client.debugEntries.isCurrentlyEnabled(DEBUG);

        if(player == null) {
            return;
        }

        if(player.isSpectator() && !alwaysRender) {
            return;
        }

        if(!(client.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }

        var validPlacement = true;

        if(hitResult.getType() == HitResult.Type.MISS) {
            validPlacement = false;

            if(!alwaysRender) {
                return;
            }
        }

        var stack = player.getMainHandItem();

        if(stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
            return;
        }

        var extractor = EXTRACTOR_MAP.getOrDefault(stack.getItem(), DEFAULT_EXTRACTOR);
        var blockCount = extractor.getBlockCount(stack);

        if(blockCount <= 0) {
            return;
        }

        if(!stack.isItemEnabled(level.enabledFeatures())) {
            validPlacement = false;
        }

        var originalPlaceContext = new BlockPlaceContext(
                level, // TODO: Preferably this would be a fake level to disallow mutations
                player,
                InteractionHand.MAIN_HAND,
                stack.copy(),
                hitResult
        );

        var collisionContext = CollisionContext.placementContext(player);
        var context = extractor.updateContext(originalPlaceContext, collisionContext);

        if(context == null) {
            validPlacement = false;
            context = originalPlaceContext;
        }

        var alwaysOnTop = GhostLevel.create(level, true);
        var afterTerrain = GhostLevel.create(level, false);

        for(var i = 0; i < blockCount; i++) {
            var blockIsValid = validPlacement;
            var blockState = extractor.getBlockState(context, collisionContext, i);

            if(blockState == null) {
                //blockIsValid = false;
                blockState = extractor.getDefaultBlockState(context, collisionContext, i);
            }

            var pos = extractor.getPos(context, collisionContext, blockState, i);
            blockState = extractor.applyComponents(context, collisionContext, pos, blockState, i);

            if(!context.canPlace() || !extractor.canPlaceAt(context, collisionContext, pos, blockState, i)) {
                blockIsValid = false;
            }

            (blockIsValid ? afterTerrain : alwaysOnTop).setBlock(pos, new GhostBlockState(
                    blockState,
                    extractor.getModelData(context, collisionContext, pos, blockState, i),
                    extractor.getSeed(context, collisionContext, pos, blockState, i),
                    extractor.getOffset(context, collisionContext, pos, blockState, i)
            ));
        }

        // TODO: only submit non-empty levels
        levelRenderState.setRenderData(ALYWAYS_ON_TOP_KEY, alwaysOnTop);
        levelRenderState.setRenderData(AFTER_TERRAIN_KEY, afterTerrain);
    }

    private static void submit(
            SubmitNodeCollector nodeCollector,
            PoseStack poseStack,
            LevelRenderState levelRenderState,
            ContextKey<GhostLevel> key
    ) {
        var ghostLevel = levelRenderState.getRenderData(key);

        if(ghostLevel != null) {
            GhostUtil.submit(
                    nodeCollector,
                    poseStack,
                    levelRenderState.cameraRenderState.pos,
                    ghostLevel
            );
        }
    }
}
