package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import dev.apexstudios.apexcore.lib.component.block.entity.types.InventoryBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.types.MultiBlockComponent;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public interface BlockEntityComponentHelper {
    String NBT_COMPONENTS = "Components";

    // region: Callbacks
    static void playerDestroy(BlockEntityComponentHolder holder, Level level, Player player, BlockPos pos, BlockState blockState, ItemStack stack) {
        holder.getComponents().forEach(component -> component.playerDestroy(level, player, pos, blockState, stack));
    }

    static void setPlacedBy(BlockEntityComponentHolder holder, Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        holder.getComponents().forEach(component -> component.setPlacedBy(level, pos, blockState, placer, stack));
    }

    static BlockState playerWillDestroy(BlockEntityComponentHolder holder, Level level, BlockPos pos, BlockState blockState, Player player) {
        var result = blockState;

        for(var component : holder.getComponents()) {
            result = component.playerWillDestroy(level, pos, result, player);
        }

        return result;
    }

    static BlockState updateShape(BlockEntityComponentHolder holder, BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        var result = blockState;

        for(var component : holder.getComponents()) {
            result = component.updateShape(result, level, tickAccess, pos, facing, neighborPos, neighborBlockState, random);
        }

        return result;
    }

    static void neighborChanged(BlockEntityComponentHolder holder, BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        holder.getComponents().forEach(component -> component.neighborChanged(blockState, level, pos, neighborBlock, orientation, movedByPiston));
    }

    static void onPlace(BlockEntityComponentHolder holder, BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        holder.getComponents().forEach(component -> component.onPlace(blockState, level, pos, oldBlockState, movedByPiston));
    }

    static void preRemoveSideEffects(BlockEntityComponentHolder holder, BlockPos pos, BlockState blockState) {
        holder.getComponents().forEach(component -> component.preRemoveSideEffects(pos, blockState));
    }

    static InteractionResult useItemOn(BlockEntityComponentHolder holder, ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        for(var component : holder.getComponents()) {
            var interactionResult = component.useItemOn(stack, blockState, level, pos, player, hand, result);

            if(interactionResult.consumesAction())
                return interactionResult;
        }

        return InteractionResult.PASS;
    }

    static InteractionResult useWithoutItem(BlockEntityComponentHolder holder, BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        for(var component : holder.getComponents()) {
            var interactionResult = component.useWithoutItem(blockState, level, pos, player, result);

            if(interactionResult.consumesAction())
                return interactionResult;
        }

        return InteractionResult.PASS;
    }

    static int getAnalogOutputSignal(BlockEntityComponentHolder holder, BlockState blockState, Level level, BlockPos pos) {
        var result = -1;

        for(var component : holder.getComponents()) {
            var signal = component.getAnalogOutputSignal(blockState, level, pos);

            if(signal > 0)
                result += signal;
        }

        return result;
    }

    static void saveAdditional(BlockEntityComponentHolder holder, CompoundTag tag, HolderLookup.Provider registries) {
        if(!shouldSerialize(holder))
            return;

        var componentsTag = new CompoundTag();

        holder.getComponentTypes().forEach(componentType -> {
            var componentTag = new CompoundTag();
            holder.getComponentOrThrow(componentType).saveNbt(componentTag, registries);

            if(!componentTag.isEmpty())
                componentsTag.put(componentType.registryName().toString(), componentTag);
        });

        if(!componentsTag.isEmpty())
            tag.put(NBT_COMPONENTS, componentsTag);
    }

    static void loadAdditional(BlockEntityComponentHolder holder, CompoundTag tag, HolderLookup.Provider registries) {
        if(shouldSerialize(holder) && tag.contains(NBT_COMPONENTS)) {
            var componentsTag = tag.getCompoundOrEmpty(NBT_COMPONENTS);

            holder.getComponentTypes().forEach(componentType -> {
                var key = componentType.registryName().toString();

                if(componentsTag.contains(key)) {
                    var componentTag = componentsTag.getCompoundOrEmpty(key);
                    holder.getComponentOrThrow(componentType).loadNbt(componentTag, registries);
                }
            });

            tag.remove(NBT_COMPONENTS);
        }
    }

    static boolean shouldSerialize(BlockEntityComponentHolder holder) {
        var blockState = holder.unwrap().getBlockState();
        var multiBlock = BlockComponentHelper.getComponent(blockState, BlockComponentTypes.MULTI_BLOCK);
        return multiBlock == null || multiBlock.indexOf(blockState) == MultiBlockComponent.ORIGIN_INDEX;
    }

    static boolean triggerEvent(BlockEntityComponentHolder holder, int id, int event) {
        for(var component : holder.getComponents()) {
            if(component.triggerEvent(id, event))
                return true;
        }

        return false;
    }

    static void applyImplicitComponents(BlockEntityComponentHolder holder, DataComponentGetter getter) {
        holder.getComponents().forEach(component -> component.applyImplicitComponents(getter));
    }

    static void collectImplicitComponents(BlockEntityComponentHolder holder, DataComponentMap.Builder components) {
        holder.getComponents().forEach(component -> component.collectImplicitComponents(components));
    }

    static void removeComponentsFromTag(BlockEntityComponentHolder holder, CompoundTag tag) {
        if(shouldSerialize(holder) && tag.contains(NBT_COMPONENTS)) {
            var componentsTag = tag.getCompoundOrEmpty(NBT_COMPONENTS);

            holder.getComponentTypes().forEach(componentType -> {
                var key = componentType.registryName().toString();

                if(componentsTag.contains(key)) {
                    var componentTag = componentsTag.getCompoundOrEmpty(key);
                    holder.getComponentOrThrow(componentType).removeComponentsFromTag(componentTag);

                    if(componentTag.isEmpty())
                        componentsTag.remove(key);
                }
            });

            if(componentsTag.isEmpty())
                tag.remove(NBT_COMPONENTS);
        }
    }

    static void entityInside(BlockEntityComponentHolder holder, BlockState blockState, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier) {
        holder.getComponents().forEach(component -> component.entityInside(blockState, level, pos, entity, applier));
    }

    static void handlePrecipitation(BlockEntityComponentHolder holder, BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        holder.getComponents().forEach(component -> component.handlePrecipitation(blockState, level, pos, precipitation));
    }

    static void stepOn(BlockEntityComponentHolder holder, Level level, BlockPos pos, BlockState blockState, Entity entity) {
        holder.getComponents().forEach(component -> component.stepOn(level, pos, blockState, entity));
    }

    static boolean updateEntityMovementAfterFallOn(BlockEntityComponentHolder holder, BlockGetter level, Entity entity) {
        for(var component : holder.getComponents()) {
            if(component.updateEntityMovementAfterFallOn(level, entity))
                return true;
        }

        return false;
    }

    static void modifyCloneItemStack(BlockEntityComponentHolder holder, ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData) {
        for(var component : holder.getComponents()) {
            component.modifyCloneItemStack(stack, level, pos, blockState, includeData);
        }
    }

    static void modifyCloneItemStack(BlockEntityComponentHolder holder, ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        for(var component : holder.getComponents()) {
            component.modifyCloneItemStack(stack, level, pos, blockState, includeData, player);
        }
    }
    // endregion

    // region: BlockGetter
    @Nullable
    static <TComponent extends BlockEntityComponent> TComponent getComponent(BlockGetter level, BlockPos pos, BlockEntityComponentType<TComponent, ?> componentType) {
        var holder = asHolder(level, pos);
        return holder == null ? null : holder.getComponent(componentType);
    }

    static <TComponent extends BlockEntityComponent> Optional<TComponent> findComponent(BlockGetter level, BlockPos pos, BlockEntityComponentType<TComponent, ?> componentType) {
        var holder = asHolder(level, pos);
        return holder == null ? Optional.empty() : holder.findComponent(componentType);
    }

    static <TComponent extends BlockEntityComponent> TComponent getComponentOrThrow(BlockGetter level, BlockPos pos, BlockEntityComponentType<TComponent, ?> componentType) {
        return asHolderOrThrow(level, pos).getComponentOrThrow(componentType);
    }

    static <TComponent extends BlockEntityComponent> void runForComponent(BlockGetter level, BlockPos pos, BlockEntityComponentType<TComponent, ?> componentType, Consumer<TComponent> action) {
        var holder = asHolder(level, pos);

        if(holder != null)
            holder.runForComponent(componentType, action);
    }

    static boolean hasComponent(BlockGetter level, BlockPos pos, BlockEntityComponentType<? extends BlockEntityComponent, ?> componentType) {
        var holder = asHolder(level, pos);
        return holder != null && holder.hasComponent(componentType);
    }

    static Set<BlockEntityComponentType<? extends BlockEntityComponent, ?>> getComponentTypes(BlockGetter level, BlockPos pos) {
        var holder = asHolder(level, pos);
        return holder == null ? Collections.emptySet() : holder.getComponentTypes();
    }

    static Collection<BlockEntityComponent> getComponents(BlockGetter level, BlockPos pos) {
        var holder = asHolder(level, pos);
        return holder == null ? Collections.emptyList() : holder.getComponents();
    }

    @Nullable
    static BlockEntityComponentHolder asHolder(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BlockEntityComponentHolder holder ? holder : null;
    }

    static BlockEntityComponentHolder asHolderOrThrow(BlockGetter level, BlockPos pos) {
        return Objects.requireNonNull(asHolder(level, pos));
    }
    // endregion

    static void registerInventoryComponents(ComponentRegistrar<BlockEntityComponent, BlockEntity, BlockEntityComponentHolder, BlockEntityComponentType<? extends BlockEntityComponent, ?>, ?> registrar, UnaryOperator<InventoryBlockEntityComponent.Builder> inventoryBuilder) {
        registrar.register(BlockEntityComponentTypes.INVENTORY, inventoryBuilder);
        registrar.register(BlockEntityComponentTypes.LOOT_TABLE);
        registrar.register(BlockEntityComponentTypes.LOCK);
        registrar.register(BlockEntityComponentTypes.NAMEABLE);
    }
}
