package dev.apexstudios.apexcore.lib.placement;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public sealed abstract class PlacementRenderEvent extends Event {
    public static final class DefaultBlockState extends PlacementRenderEvent {
        private final LevelReader realLevel;
        private final BlockPlaceContext placeContext;
        private final BlockState defaultBlockState;
        @Nullable private BlockState newDefaultBlockState;

        private DefaultBlockState(LevelReader realLevel, BlockPlaceContext placeContext, BlockState defaultBlockState) {
            this.realLevel = realLevel;
            this.placeContext = placeContext;
            this.defaultBlockState = defaultBlockState;
        }

        public LevelReader level() {
            return realLevel;
        }

        public BlockPos pos() {
            return placeContext.getClickedPos();
        }

        public BlockPlaceContext placeContext() {
            return placeContext;
        }

        public BlockState defaultBlockState() {
            return newDefaultBlockState == null ? defaultBlockState : newDefaultBlockState;
        }

        public void setDefaultBlockState(BlockState defaultBlockState) {
            newDefaultBlockState = defaultBlockState;
        }

        public <TProperty extends Comparable<TProperty>> void withProperty(Property<TProperty> property, Supplier<TProperty> value) {
            var blockState = defaultBlockState();

            if(blockState.hasProperty(property))
                setDefaultBlockState(blockState.trySetValue(property, value.get()));
        }
    }

    public static final class ModifyBlockState extends PlacementRenderEvent {
        private final LevelReader realLevel;
        private final BlockPlaceContext placeContext;
        private final BlockState originalBlockState;
        @Nullable private BlockState newBlockState;

        private ModifyBlockState(LevelReader realLevel, BlockPlaceContext placeContext, BlockState originalBlockState) {
            this.realLevel = realLevel;
            this.placeContext = placeContext;
            this.originalBlockState = originalBlockState;
        }

        public LevelReader level() {
            return realLevel;
        }

        public BlockPos pos() {
            return placeContext.getClickedPos();
        }

        public BlockPlaceContext placeContext() {
            return placeContext;
        }

        public BlockState originalBlockState() {
            return newBlockState == null ? originalBlockState : newBlockState;
        }

        public void setBlockState(BlockState blockState) {
            newBlockState = blockState;
        }

        public <TProperty extends Comparable<TProperty>> void withProperty(Property<TProperty> property, Supplier<TProperty> value) {
            var blockState = originalBlockState();

            if(blockState.hasProperty(property))
                setBlockState(blockState.trySetValue(property, value.get()));
        }
    }

    public static final class Register extends PlacementRenderEvent implements IModBusEvent {
        private final Consumer<BlockPlacementRenderer> registrar;

        private Register(Consumer<BlockPlacementRenderer> registrar) {
            this.registrar = registrar;
        }

        public void register(BlockPlacementRenderer renderer) {
            registrar.accept(renderer);
        }
    }

    @ApiStatus.Internal
    public static BlockState getDefaultBlockState(LevelReader realLevel, BlockPlaceContext placeContext, BlockState defaultBlockState) {
        return NeoForge.EVENT_BUS.post(new DefaultBlockState(realLevel, placeContext, defaultBlockState)).defaultBlockState();
    }

    @ApiStatus.Internal
    public static BlockState modifyBlockState(LevelReader realLevel, BlockPlaceContext placeContext, BlockState originalBlockState) {
        return NeoForge.EVENT_BUS.post(new ModifyBlockState(realLevel, placeContext, originalBlockState)).originalBlockState();
    }

    @ApiStatus.Internal
    public static Iterable<BlockPlacementRenderer> registerRenderers(Consumer<Consumer<BlockPlacementRenderer>> registerBuiltIn) {
        var registry = Lists.<BlockPlacementRenderer>newLinkedList();
        registerBuiltIn.accept(registry::add);
        ModLoader.postEventWrapContainerInModOrder(new Register(registry::add));
        return List.copyOf(registry);
    }
}
