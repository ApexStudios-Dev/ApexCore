package dev.apexstudios.apexcore.core.placement;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementState;
import dev.apexstudios.apexcore.lib.placement.RegisterBlockPlacementRendererEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public interface PlacementRendererRegistry {
    ContextKey<RenderState> RENDER_STATE_KEY = new ContextKey<>(ApexCore.identifier("block_placement_render_state"));

    static void register() {
        var renderers = RegisterBlockPlacementRendererEvent.register(event -> {
            BlockItemPlacementRenderer.register(event);
        });

        NeoForge.EVENT_BUS.addListener(ExtractLevelRenderStateEvent.class, event -> {
            var levelState = event.getRenderState();
            var level = event.getLevel();

            var client = Minecraft.getInstance();
            assert client.player != null;
            var alwaysRender = true;

            if(!(client.hitResult instanceof BlockHitResult hitResult) || (hitResult.getType() != HitResult.Type.BLOCK && !alwaysRender))
                return;

            if(PlacementRendererRegistry.extractForHand(renderers.values(), level, levelState, client.player, InteractionHand.MAIN_HAND, hitResult))
                return;

            PlacementRendererRegistry.extractForHand(renderers.values(), level, levelState, client.player, InteractionHand.OFF_HAND, hitResult);
        });

        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterOpaqueBlocks.class, event -> {
            var levelState = event.getLevelRenderState();
            var placementState = levelState.getRenderData(RENDER_STATE_KEY);

            if(placementState == null)
                return;

            var renderer = renderers.get(placementState.key);

            if(renderer == null)
                return;

            var collector = Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();
            var pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(-levelState.cameraRenderState.pos.x, -levelState.cameraRenderState.pos.y, -levelState.cameraRenderState.pos.z);

            renderer.submit(levelState, placementState, pose, collector);

            pose.popPose();
        });
    }

    private static boolean extractForHand(Iterable<BlockPlacementRender<?>> renderers, Level level, LevelRenderState levelState, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var placementState = new BlockPlacementStateImpl(player, hand, hitResult, level.enabledFeatures());

        for(var renderer : renderers) {
            if(renderer.extract(level, levelState, player, hitResult, placementState)) {
                levelState.setRenderData(RENDER_STATE_KEY, new RenderState(renderer.key(), placementState));
                return true;
            }
        }

        return false;
    }

    record BlockPlacementStateImpl(InteractionHand hand, ItemStack stack, BlockPos pos, Direction face, FeatureFlagSet enabledFeatures) implements BlockPlacementState {
        public BlockPlacementStateImpl(Player player, InteractionHand hand, BlockHitResult hitResult, FeatureFlagSet enabledFeatures) {
            this(hand, player.getItemInHand(hand).copy(), hitResult.getBlockPos(), hitResult.getDirection(), enabledFeatures);
        }
    }

    record RenderState(ContextKey<?> key, BlockPlacementState placementState) implements BlockPlacementState {
        @Override
        public ItemStack stack() {
            return placementState.stack();
        }

        @Override
        public InteractionHand hand() {
            return placementState.hand();
        }

        @Override
        public BlockPos pos() {
            return placementState.pos();
        }

        @Override
        public Direction face() {
            return placementState.face();
        }

        @Override
        public FeatureFlagSet enabledFeatures() {
            return placementState.enabledFeatures();
        }
    }
}
