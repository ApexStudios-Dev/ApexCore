package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.ComponentHelper;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface BlockComponentHelper {
    // region: Callbacks
    @ApiStatus.Internal
    static <THolder extends Block & ComponentHolder<BlockComponent>> Map<ComponentType<BlockComponent, ?, ?>, BlockComponent> registerComponents(THolder holder, BiConsumer<THolder, ComponentRegistrar<BlockComponent>> consumer) {
        var map = ComponentHelper.registerComponents(holder, consumer);

        // patch the state definition
        // this allows components to register state properties
        // and define their own default state values
        ApexUtil.replaceBlockStateDefinition(
                holder,
                properties -> map.values().forEach(component -> component.createBlockStateDefinition(properties)),
                defaultBlockState -> {
                    for(var component : map.values()) {
                        defaultBlockState = component.registerDefaultBlockState(defaultBlockState);
                    }

                    return defaultBlockState;
                }
        );

        return map;
    }

    @Nullable
    static BlockState getStateForPlacement(ComponentHolder<BlockComponent> holder, BlockPlaceContext context, BlockState blockState) {
        var result = blockState;

        for(var component : holder.getComponents()) {
            result = component.getStateForPlacement(context, result);

            if(result == null)
                return null;
        }

        return result;
    }

    static void playerDestroy(ComponentHolder<BlockComponent> holder, Level level, Player player, BlockPos pos, BlockState blockState, ItemStack stack) {
        holder.getComponents().forEach(component -> component.playerDestroy(level, player, pos, blockState, stack));
    }

    static void setPlacedBy(ComponentHolder<BlockComponent> holder, Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        holder.getComponents().forEach(component -> component.setPlacedBy(level, pos, blockState, placer, stack));
    }

    static BlockState playerWillDestroy(ComponentHolder<BlockComponent> holder, Level level, BlockPos pos, BlockState blockState, Player player) {
        var result = blockState;

        for(var component : holder.getComponents()) {
            result = component.playerWillDestroy(level, pos, blockState, player);
        }

        return result;
    }

    static BlockState updateShape(ComponentHolder<BlockComponent> holder, BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        var result = blockState;

        for(var component : holder.getComponents()) {
            result = component.updateShape(result, level, tickAccess, pos, facing, neighborPos, neighborBlockState, random);
        }

        return result;
    }

    static void neighborChanged(ComponentHolder<BlockComponent> holder, BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        holder.getComponents().forEach(component -> component.neighborChanged(blockState, level, pos, neighborBlock, orientation, movedByPiston));
    }

    static void onPlace(ComponentHolder<BlockComponent> holder, BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        holder.getComponents().forEach(component -> component.onPlace(blockState, level, pos, oldBlockState, movedByPiston));
    }

    static void onRemove(ComponentHolder<BlockComponent> holder, BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean movedByPiston) {
        holder.getComponents().forEach(component -> component.onRemove(blockState, level, pos, newBlockState, movedByPiston));
    }

    static InteractionResult useItemOn(ComponentHolder<BlockComponent> holder, ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        for(var component : holder.getComponents()) {
            var interactionResult = component.useItemOn(stack, blockState, level, pos, player, hand, result);

            if(interactionResult.consumesAction())
                return interactionResult;
        }

        return InteractionResult.PASS;
    }

    static InteractionResult useWithoutItem(ComponentHolder<BlockComponent> holder, BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        for(var component : holder.getComponents()) {
            var interactionResult = component.useWithoutItem(blockState, level, pos, player, result);

            if(interactionResult.consumesAction())
                return interactionResult;
        }

        return InteractionResult.PASS;
    }

    static int getAnalogOutputSignal(ComponentHolder<BlockComponent> holder, BlockState blockState, Level level, BlockPos pos) {
        var result = 0;

        for(var component : holder.getComponents()) {
            var signal = component.getAnalogOutputSignal(blockState, level, pos);

            if(signal > 0)
                result += signal;
        }

        return result;
    }

    static boolean hasAnalogOutputSignal(ComponentHolder<BlockComponent> holder, BlockState blockState) {
        for(var component : holder.getComponents()) {
            if(component.hasAnalogOutputSignal(blockState))
                return true;
        }

        return false;
    }

    static boolean isPathfindable(ComponentHolder<BlockComponent> holder, BlockState blockState, PathComputationType pathType) {
        for(var component : holder.getComponents()) {
            if(!component.isPathfindable(blockState, pathType))
                return false;
        }

        return true;
    }

    static void tick(ComponentHolder<BlockComponent> holder, BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        holder.getComponents().forEach(component -> component.tick(blockState, level, pos, random));
    }

    static void entityInside(ComponentHolder<BlockComponent> holder, BlockState blockState, Level level, BlockPos pos, Entity entity) {
        holder.getComponents().forEach(component -> component.entityInside(blockState, level, pos, entity));
    }

    static void handlePrecipitation(ComponentHolder<BlockComponent> holder, BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        holder.getComponents().forEach(component -> component.handlePrecipitation(blockState, level, pos, precipitation));
    }

    static void stepOn(ComponentHolder<BlockComponent> holder, Level level, BlockPos pos, BlockState blockState, Entity entity) {
        holder.getComponents().forEach(component -> component.stepOn(level, pos, blockState, entity));
    }

    static BlockState rotate(ComponentHolder<BlockComponent> holder, BlockState blockState, Rotation rotation) {
        var result = blockState;

        for(var component : holder.getComponents()) {
            result = component.rotate(blockState, rotation);
        }

        return result;
    }

    static BlockState mirror(ComponentHolder<BlockComponent> holder, BlockState blockState, Mirror mirror) {
        var result = blockState;

        for(var component : holder.getComponents()) {
            result = component.mirror(blockState, mirror);
        }

        return result;
    }

    static FluidState getFluidState(ComponentHolder<BlockComponent> holder, BlockState blockState, FluidState defaultFluidState) {
        var fluidState = defaultFluidState;

        for(var component : holder.getComponents()) {
            fluidState = component.getFluidState(blockState, fluidState);
        }

        return fluidState;
    }

    static void onExplosionHit(ComponentHolder<BlockComponent> holder, BlockState blockState, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropsConsumer) {
        holder.getComponents().forEach(component -> component.onExplosionHit(blockState, level, pos, explosion, dropsConsumer));
    }
    // endregion

    // region: BlockState
    @Nullable
    static <TComponent extends BlockComponent> TComponent getComponent(BlockState blockState, ComponentType<BlockComponent, TComponent, ?> componentType) {
        var holder = asHolder(blockState);
        return holder == null ? null : holder.getComponent(componentType);
    }

    static <TComponent extends BlockComponent> Optional<TComponent> findComponent(BlockState blockState, ComponentType<BlockComponent, TComponent, ?> componentType) {
        var holder = asHolder(blockState);
        return holder == null ? Optional.empty() : holder.findComponent(componentType);
    }

    static <TComponent extends BlockComponent> TComponent getComponentOrThrow(BlockState blockState, ComponentType<BlockComponent, TComponent, ?> componentType) {
        return asHolderOrThrow(blockState).getComponentOrThrow(componentType);
    }

    static <TComponent extends BlockComponent> void runForComponent(BlockState blockState, ComponentType<BlockComponent, TComponent, ?> componentType, Consumer<TComponent> action) {
        var holder = asHolder(blockState);

        if(holder != null)
            holder.runForComponent(componentType, action);
    }

    static boolean hasComponent(BlockState blockState, ComponentType<BlockComponent, ?, ?> componentType) {
        var holder = asHolder(blockState);
        return holder != null && holder.hasComponent(componentType);
    }

    static Set<ComponentType<BlockComponent, ?, ?>> getComponentTypes(BlockState blockState) {
        var holder = asHolder(blockState);
        return holder == null ? Collections.emptySet() : holder.getComponentTypes();
    }

    static Collection<BlockComponent> getComponents(BlockState blockState) {
        var holder = asHolder(blockState);
        return holder == null ? Collections.emptyList() : holder.getComponents();
    }

    @Nullable
    static ComponentHolder<BlockComponent> asHolder(BlockState blockState) {
        var block = blockState.getBlock();
        return block instanceof ComponentHolder ? (ComponentHolder<BlockComponent>) block : null;
    }

    static ComponentHolder<BlockComponent> asHolderOrThrow(BlockState blockState) {
        return (ComponentHolder<BlockComponent>) blockState.getBlock();
    }
    // endregion
}
