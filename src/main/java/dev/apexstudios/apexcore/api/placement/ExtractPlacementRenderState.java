package dev.apexstudios.apexcore.api.placement;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

public final class ExtractPlacementRenderState extends Event {
    public final BlockPlaceContext placeContext;

    private final Long2ObjectMap<BlockState> blockStates;

    @ApiStatus.Internal
    public ExtractPlacementRenderState(BlockPlaceContext placeContext, Long2ObjectMap<BlockState> blockStates) {
        this.placeContext = placeContext;
        this.blockStates = blockStates;
    }

    public BlockState blockState() {
        return get(placeContext.getClickedPos());
    }

    public BlockState get(BlockPos pos) {
        return blockStates.getOrDefault(pos.asLong(), Blocks.AIR.defaultBlockState());
    }

    public void put(BlockPos pos, BlockState blockState) {
        if(blockState.isAir()) {
            remove(pos);
        } else {
            blockStates.put(pos.asLong(), blockState);
        }
    }

    public void put(BlockPos pos, Block block) {
        put(pos, block.defaultBlockState());
    }

    public void remove(BlockPos pos) {
        blockStates.remove(pos.asLong());
    }
}
