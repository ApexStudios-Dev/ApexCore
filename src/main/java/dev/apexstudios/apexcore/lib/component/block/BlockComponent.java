package dev.apexstudios.apexcore.lib.component.block;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.block.BlockEvents;
import dev.apexstudios.apexcore.lib.component.Component;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

public interface BlockComponent extends Component<BlockComponent>, ComponentHolder<BlockComponent>, BlockEvents {
    @ForOverride
    default BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState;
    }

    @ForOverride
    default void createBlockStateDefinition(Consumer<Property<?>> consumer) {

    }

    @ForOverride
    @Nullable
    default BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        return blockState;
    }

    @ForOverride
    default boolean hasAnalogOutputSignal(BlockState blockState) {
        return false;
    }

    @ForOverride
    default boolean isPathfindable(BlockState blockState, PathComputationType pathType) {
        return true;
    }

    @ForOverride
    default void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {

    }

    @ForOverride
    default BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState;
    }

    @ForOverride
    default BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState;
    }

    @ForOverride
    default FluidState getFluidState(BlockState blockState, FluidState fluidState) {
        return fluidState;
    }

    @ForOverride
    default void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropsConsumer) {

    }
}
