package dev.apexstudios.apexcore.api.block.behavior;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import dev.apexstudios.apexcore.mixin.BlockAccessor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public final class BlockBehaviorManager implements BlockBehaviorAccess {
    private final Map<BlockBehaviorType<?, ?>, BlockBehavior> behaviors;

    public <TBlock extends Block & IBehaviorBlock> BlockBehaviorManager(TBlock block, Consumer<BlockBehaviorRegistrar> registrarCallback) {
        var registration = new BlockBehaviorRegistration(block);
        behaviors = register(registration, registrarCallback);
        injectBlockProperties(registration, block);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TBehavior extends BlockBehavior> @Nullable TBehavior getBehavior(BlockBehaviorType<TBehavior, ?> type) {
        return (TBehavior) behaviors.get(type);
    }

    @Override
    public boolean hasBehavior(BlockBehaviorType<?, ?> type) {
        return behaviors.containsKey(type);
    }

    @Override
    public Collection<BlockBehavior> getBehaviors() {
        return behaviors.values();
    }

    private static Map<BlockBehaviorType<?, ?>, BlockBehavior> register(BlockBehaviorRegistration registration, Consumer<BlockBehaviorRegistrar> registrarCallback) {
        var registrar = new BlockBehaviorRegistrar() {
            private final Multimap<BlockBehaviorType<?, ?>, Consumer<?>> callbacks = HashMultimap.create();

            @Override
            public <TBehavior extends BlockBehavior, TProperties> BlockBehaviorRegistrar register(BlockBehaviorType<TBehavior, TProperties> type, Consumer<TProperties> propertiesCallback) {
                callbacks.put(type, propertiesCallback);
                return this;
            }
        };

        registrarCallback.accept(registrar);

        if(registrar.callbacks.isEmpty()) {
            return Collections.emptyMap();
        }

        var map = ImmutableMap.<BlockBehaviorType<?, ?>, BlockBehavior>builderWithExpectedSize(registrar.callbacks.size());

        for(var type : registrar.callbacks.keySet()) {
            map.put(type, createBehavior(registration, type, registrar.callbacks.get(type)));
        }

        return map.buildOrThrow();
    }

    @SuppressWarnings("unchecked")
    private static <TBehavior extends BlockBehavior, TProperties> TBehavior createBehavior(BlockBehaviorRegistration registration, BlockBehaviorType<TBehavior, TProperties> type, Collection<Consumer<?>> callbacks) {
        return type.create(registration, properties -> callbacks.forEach(callback -> ((Consumer<TProperties>) callback).accept(properties)));
    }

    private static void injectBlockProperties(BlockBehaviorRegistration registration, Block block) {
        if(registration.properties.isEmpty()) {
            return;
        }

        var accessor = (BlockAccessor) block;
        var originalStateDefinition = block.getStateDefinition();
        var originalDefaultBlockState = block.defaultBlockState();

        var stateDefinitionBuilder = new StateDefinition.Builder<Block, BlockState>(block);
        accessor.ApexCore$createBlockStateDefinition(stateDefinitionBuilder);
        stateDefinitionBuilder.add(registration.properties.keySet().toArray(Property[]::new));

        var stateDefinition = stateDefinitionBuilder.create(Block::defaultBlockState, BlockState::new);
        accessor.ApexCore$setStateDefinition(stateDefinition);
        var defaultBlockState = stateDefinition.any();

        for(var property : originalStateDefinition.getProperties()) {
            defaultBlockState = Block.copyProperty(originalDefaultBlockState, defaultBlockState, property);
        }

        for(var property : registration.properties.keySet()) {
            defaultBlockState = withProperty(defaultBlockState, property, registration);
        }

        accessor.ApexCore$registerDefaultState(defaultBlockState);
    }

    @SuppressWarnings("unchecked")
    private static <TValue extends Comparable<TValue>> BlockState withProperty(BlockState blockState, Property<TValue> property, BlockBehaviorRegistration registration) {
        return blockState.setValue(property, (TValue) registration.properties.get(property));
    }
}
