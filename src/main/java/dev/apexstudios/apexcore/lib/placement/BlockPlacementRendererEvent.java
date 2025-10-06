package dev.apexstudios.apexcore.lib.placement;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public sealed class BlockPlacementRendererEvent extends Event {
    private final BlockPlacementRenderContext renderContext;

    protected BlockPlacementRendererEvent(BlockPlacementRenderContext renderContext) {
        this.renderContext = renderContext;
    }

    public final BlockPlacementRenderContext getRenderContext() {
        return renderContext;
    }

    public static final class UpdatePlacementContext extends BlockPlacementRendererEvent implements ICancellableEvent {
        private BlockPlaceContext placeContext;

        @ApiStatus.Internal
        public UpdatePlacementContext(BlockPlacementRenderContext renderContext) {
            super(renderContext);

            placeContext = new BlockPlaceContext(renderContext.level(), renderContext.player(), renderContext.hand(), renderContext.item().toStack(), renderContext.hitResult());
        }

        public void setPlaceContext(@Nullable BlockPlaceContext placeContext) {
            if(placeContext == null)
                setCanceled(true);
            else
                this.placeContext = placeContext;
        }

        public BlockPlaceContext getPlaceContext() {
            return placeContext;
        }
    }

    public static final class GetBlock extends BlockPlacementRendererEvent {
        private Block block;

        @ApiStatus.Internal
        public GetBlock(BlockPlacementRenderContext renderContext) {
            super(renderContext);

            block = Block.byItem(renderContext.item().value());
        }

        public void setBlock(Block block) {
            this.block = block;
        }

        public Block getBlock() {
            return block;
        }
    }

    public static sealed class GetBlockState extends BlockPlacementRendererEvent {
        private final BlockPlaceContext placeContext;
        private BlockState blockState;

        private GetBlockState(BlockPlacementRenderContext renderContext, BlockPlaceContext placeContext, BlockState blockState) {
            super(renderContext);

            this.placeContext = placeContext;
            this.blockState = blockState;
        }

        public final BlockPlaceContext getPlaceContext() {
            return placeContext;
        }

        public final BlockState getBlockState() {
            return blockState;
        }

        @OverridingMethodsMustInvokeSuper
        public void setBlockState(BlockState blockState) {
            this.blockState = blockState;
        }
    }

    public static final class GetDefaultBlockState extends GetBlockState {
        @ApiStatus.Internal
        public GetDefaultBlockState(BlockPlacementRenderContext renderContext, BlockPlaceContext placeContext, Block block) {
            super(renderContext, placeContext, block.defaultBlockState());
        }
    }

    public static final class GetPlacementBlockState extends GetBlockState implements ICancellableEvent {
        @ApiStatus.Internal
        public GetPlacementBlockState(BlockPlacementRenderContext renderContext, BlockPlaceContext placeContext, BlockState defaultBlockState) {
            super(renderContext, placeContext, getInitialBlockState(placeContext, defaultBlockState));
        }

        @Override
        public void setBlockState(@Nullable BlockState blockState) {
            if(blockState == null)
                setCanceled(true);
            else
                super.setBlockState(blockState);
        }

        private static BlockState getInitialBlockState(BlockPlaceContext placeContext, BlockState defaultBlockState) {
            var placementBlockState = defaultBlockState.getBlock().getStateForPlacement(placeContext);
            return placementBlockState == null ? defaultBlockState : placementBlockState;
        }
    }

    public static final class CollectAdditionalBlockStates extends BlockPlacementRendererEvent {
        private final BlockPlaceContext placeContext;
        private final BlockState blockState;
        private final Long2ObjectMap<BlockState> additionalBlockStates;

        @ApiStatus.Internal
        public CollectAdditionalBlockStates(BlockPlacementRenderContext renderContext, BlockPlaceContext placeContext, BlockState blockState, Long2ObjectMap<BlockState> additionalBlockStates) {
            super(renderContext);

            this.placeContext = placeContext;
            this.blockState = blockState;
            this.additionalBlockStates = additionalBlockStates;
        }

        public BlockPlaceContext getPlaceContext() {
            return placeContext;
        }

        public BlockState getBlockState() {
            return blockState;
        }

        public void with(BlockPos pos, BlockState blockState) {
            additionalBlockStates.put(pos.asLong(), blockState);
        }
    }

    public static final class StripInvalidProperties extends Event {
        private final BlockState defaultBlockState;
        private BlockState placementBlockState;

        @ApiStatus.Internal
        public StripInvalidProperties(BlockState defaultBlockState, BlockState placementBlockState) {
            this.defaultBlockState = defaultBlockState;
            this.placementBlockState = placementBlockState;
        }

        public BlockState getDefaultBlockState() {
            return defaultBlockState;
        }

        public BlockState getPlacementBlockState() {
            return placementBlockState;
        }

        public <T extends Comparable<T>> void strip(Property<T> property) {
            if(defaultBlockState.hasProperty(property))
                placementBlockState = copy(defaultBlockState, placementBlockState, property);
            else
                placementBlockState = copy(placementBlockState.getBlock().defaultBlockState(), placementBlockState, property);
        }

        public void strip(Property<?> property, Property<?>... properties) {
            strip(property);

            for(var prop : properties) {
                strip(prop);
            }
        }

        private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState into, Property<T> property) {
            if(!from.hasProperty(property))
                return into;

            return into.trySetValue(property, from.getValue(property));
        }
    }
}
