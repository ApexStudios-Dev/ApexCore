package dev.apexstudios.apexcore.core.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.apexcore.lib.level.FakeLevel;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementRenderer;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import dev.apexstudios.apexcore.mixin.BlockItemAccessor;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

final class BlockItemPlacementRenderer implements BlockPlacementRenderer {
    @Override
    public boolean renderForHand(Level level, Player player, InteractionHand hand, BlockHitResult hitResult, Camera camera, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        var stack = player.getItemInHand(hand);

        if(!(stack.getItem() instanceof BlockItem item))
            return false;
        if(!item.getBlock().builtInRegistryHolder().is(BLOCK_WHITELIST))
            return false;

        var canBePlaced = new AtomicBoolean(stack.isItemEnabled(level.enabledFeatures()));
        var context = buildContext(level, player, hand, stack, item, hitResult, canBePlaced);
        placeBlock(level, context, item, canBePlaced);

        if(canBePlaced.get()) {
            var contextPositions = copyContextBlockStates(level, context);
            updateContexts(context, contextPositions);
            validatePlacement(context, canBePlaced);

            // clear out context positions from level
            // we only want to render the states we are placing/updating
            contextPositions.forEach(pos -> context.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE));
        }

        BlockPlacementRenderer.renderAt(camera, pose, () -> BlockPlacementRenderer.renderLevel(context, pose, canBePlaced.get()));
        return true;
    }

    private BlockPlaceContext buildContext(Level level, Player player, InteractionHand hand, ItemStack stack, BlockItem item, BlockHitResult hitResult, AtomicBoolean canBePlaced) {
        var fakeLevel = new FakeLevel(level);
        var originalContext = new BlockPlaceContext(fakeLevel, player, hand, stack.copy(), hitResult);

        if(!originalContext.canPlace())
            canBePlaced.set(false);

        var context = item.updatePlacementContext(originalContext);

        if(context == null) {
            context = originalContext;
            canBePlaced.set(false);
        }

        return context;
    }

    private void placeBlock(LevelReader realLevel, BlockPlaceContext context, BlockItem item, AtomicBoolean canBePlaced) {
        var level = (FakeLevel) context.getLevel();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();

        var accessor = (BlockItemAccessor) item;
        var blockState = accessor.ApexCore$getPlacementState(context);

        if(blockState == null) {
            blockState = BlockPlacementRenderer.getDefaultBlockState(realLevel, context, item.getBlock().defaultBlockState());
            canBePlaced.set(false);
        }

        // place the origin block as if it came from the block item
        accessor.ApexCore$placeBlock(context, blockState);
        var fBlockState = accessor.ApexCore$updateBlockStateFromTag(pos, level, stack, blockState);
        accessor.ApexCore$updateCustomBlockEntityTag(pos, level, context.getPlayer(), stack, blockState);
        BlockItem.updateBlockEntityComponents(level, pos, stack);

        // fire block events to trigger additional block placement/updates
        level.runAsServerSide(() -> {
            // double tall/wide blocks place the other block here
            fBlockState.getBlock().setPlacedBy(level, pos, fBlockState, context.getPlayer(), stack);
            // rails update shapes here
            fBlockState.onPlace(level, pos, Blocks.AIR.defaultBlockState(), false);
        });
    }

    private List<BlockPos> copyContextBlockStates(LevelReader realLevel, BlockPlaceContext context) {
        var level = (FakeLevel) context.getLevel();
        var renderPositions = level.positions().toList();

        BlockPos.breadthFirstTraversal(context.getClickedPos(), 4, 64, (pos, childConsumer) -> {
            for(var direction : Direction.values()) {
                childConsumer.accept(pos.relative(direction));
            }

            if(!renderPositions.contains(pos))
                level.setBlock(pos, realLevel.getBlockState(pos), Block.UPDATE_NONE);
        }, pos -> BlockPos.TraversalNodeStatus.ACCEPT);

        // Stream.toList() is immutable but we want mutability
        return level.positions().filter(Predicate.not(renderPositions::contains)).collect(Collectors.toList());
    }

    private void updateContexts(BlockPlaceContext context, List<BlockPos> contextPositions) {
        var level = context.getLevel();
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

    private void validatePlacement(BlockPlaceContext context, AtomicBoolean canBePlaced) {
        var level = context.getLevel();
        var origin = context.getClickedPos();
        var player = context.getPlayer();

        var blockState = level.getBlockState(origin);
        var blockEntity = level.getBlockEntity(origin);
        level.setBlock(origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);

        if(!ApexUtil.isInBounds(level, origin))
            canBePlaced.set(false);
        else if(!ApexUtil.canPlace(level, origin, blockState, player))
            canBePlaced.set(false);

        level.setBlock(origin, blockState, Block.UPDATE_NONE);

        if(blockEntity != null)
            level.setBlockEntity(blockEntity);
    }
}
