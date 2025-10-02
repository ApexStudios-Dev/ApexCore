package dev.apexstudios.apexcore.core.placement;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.level.FakeLevel;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementState;
import dev.apexstudios.apexcore.lib.placement.GetDefaultBlockPlacementStateEvent;
import dev.apexstudios.apexcore.lib.placement.RegisterBlockPlacementRendererEvent;
import dev.apexstudios.apexcore.lib.placement.SetBlockPlacementStateEvent;
import dev.apexstudios.apexcore.lib.placement.SubmitBlockPlacementState;
import dev.apexstudios.apexcore.lib.util.ApexTags;
import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

final class BlockItemPlacementRenderer {
    private static final ContextKey<State> KEY = new ContextKey<>(ApexCore.identifier("block_item_placement_render_state"));

    static void register(RegisterBlockPlacementRendererEvent event) {
        event.register(KEY, (level, levelState, player, hitResult, placementState) -> {
            if(!(placementState.stack().getItem() instanceof BlockItem item))
                return null;
            if(!item.getBlock().builtInRegistryHolder().is(ApexTags.Blocks.RENDER_PLACEMENT_WHITELIST))
                return null;

            var state = new State(level, player, hitResult, placementState);
            placeBlock(level, player, state);

            if(state.canBePlaced) {
                var contextPositions = copyContextBlockStates(level, state);
                updateContexts(state, contextPositions);
                validatePlacement(state);

                // clear out context positions from level
                // we only want to render the states we are placing/updating
                contextPositions.forEach(pos -> state.fakeLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE));
            }

            return state;
        }, (state, levelState, placementState, pose, collector) -> {
            var level = state.fakeLevel();
            level.positions().forEach(pos -> {
                pose.pushPose();
                pose.translate(pos.getX(), pos.getY(), pos.getZ());

                SubmitBlockPlacementState.submitGhostBlock(pose, level, pos, level.getBlockState(pos), state.canBePlaced, collector);

                pose.popPose();
            });
        });
    }

    private static class State {
        public boolean canBePlaced;
        public final BlockPlaceContext placeContext;
        public final BlockItem item;

        public State(Level level, Player player, BlockHitResult hitResult, BlockPlacementState placementState) {
            var stack = placementState.stack();

            canBePlaced = stack.isItemEnabled(placementState.enabledFeatures());
            item = (BlockItem) stack.getItem();

            var placeContext = new BlockPlaceContext(new FakeLevel(level), player, placementState.hand(), stack, hitResult);

            if(!placeContext.canPlace())
                canBePlaced = false;

            var context = item.updatePlacementContext(placeContext);

            if(context == null)
                canBePlaced = false;
            else
                placeContext = context;

            this.placeContext = placeContext;
        }

        public FakeLevel fakeLevel() {
            return (FakeLevel) placeContext.getLevel();
        }
    }

    private static void placeBlock(LevelReader realLevel, Player player, State state) {
        var level = state.fakeLevel();
        var pos = state.placeContext.getClickedPos();
        var stack = state.placeContext.getItemInHand();

        var accessor = (BlockItemAccessor) state.item;
        var blockState = accessor.ApexCore$getPlacementState(state.placeContext);

        if(blockState == null) {
            blockState = GetDefaultBlockPlacementStateEvent.get(realLevel, state.placeContext, state.item.getBlock().defaultBlockState());
            state.canBePlaced = false;
        }

        blockState = SetBlockPlacementStateEvent.set(realLevel, state.placeContext, blockState);

        if(blockState.hasProperty(BlockStateProperties.WATERLOGGED))
            blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);

        // place the origin block as if it came from the block item
        accessor.ApexCore$placeBlock(state.placeContext, blockState);
        var fBlockState = accessor.ApexCore$updateBlockStateFromTag(pos, level, stack, blockState);
        level.runAsServerSide(() -> accessor.ApexCore$updateCustomBlockEntityTag(pos, level, player, stack, fBlockState));
        BlockItem.updateBlockEntityComponents(level, pos, stack);

        // fire block events to trigger additional block placement/updates
        level.runAsServerSide(() -> {
            // double tall/wide blocks place the other block here
            fBlockState.getBlock().setPlacedBy(level, pos, fBlockState, player, stack);
            // rails update shapes here
            fBlockState.onPlace(level, pos, Blocks.AIR.defaultBlockState(), false);
        });

        validatePlacement(state);
    }

    private static List<BlockPos> copyContextBlockStates(LevelReader realLevel, State state) {
        var level = state.fakeLevel();
        var renderPositions = level.positions().toList();

        BlockPos.breadthFirstTraversal(state.placeContext.getClickedPos(), 4, 64, (pos, childConsumer) -> {
            for(var direction : Direction.values()) {
                childConsumer.accept(pos.relative(direction));
            }

            if(!renderPositions.contains(pos))
                level.setBlock(pos, realLevel.getBlockState(pos), Block.UPDATE_NONE);
        }, pos -> BlockPos.TraversalNodeStatus.ACCEPT);

        // Stream.toList() is immutable but we want mutability
        return level.positions().filter(Predicate.not(renderPositions::contains)).collect(Collectors.toList());
    }

    private static void updateContexts(State state, List<BlockPos> contextPositions) {
        var level = state.fakeLevel();
        var itr = contextPositions.iterator();

        while (itr.hasNext()) {
            var pos = itr.next();
            var blockState = level.getBlockState(pos);
            var newBlockState = Block.updateFromNeighbourShapes(blockState, level, pos);

            if (newBlockState != blockState) {
                itr.remove();
                var blockEntity = level.getBlockEntity(pos);
                level.setBlock(pos, newBlockState, Block.UPDATE_NONE);

                if (blockEntity != null)
                    level.setBlockEntity(blockEntity);
            }
        }
    }

    private static void validatePlacement(State state) {
        var level = state.fakeLevel();
        var blockState = level.getBlockState(state.placeContext.getClickedPos());

        if(!((BlockItemAccessor) state.item).ApexCore$canPlace(state.placeContext, blockState))
            state.canBePlaced = false;
    }
}
