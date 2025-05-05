package dev.apexstudios.apexcore.lib.component.block;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentRegistrar;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

@ApiStatus.ScheduledForRemoval
public class BaseBlockComponentHolder extends Block implements ComponentHolder<BlockComponent, Block> {
    private final Map<ComponentType<BlockComponent, ?, Block, ?>, BlockComponent> components = BlockComponentHelper.registerComponents(this, BaseBlockComponentHolder::registerComponents);

    protected BaseBlockComponentHolder(Properties properties) {
        super(properties);
    }

    protected boolean openMenu(Level level, BlockPos pos, BlockState blockState, Player player) {
        var menuProvider = blockState.getMenuProvider(level, pos);

        if(menuProvider == null)
            return false;

        player.openMenu(menuProvider);
        return true;
    }

    // region: ComponentHolder
    @ForOverride
    protected void registerComponents(ComponentRegistrar<BlockComponent, Block> registrar) {

    }

    @Nullable
    @Override
    public final <TComponent extends BlockComponent> TComponent getComponent(ComponentType<BlockComponent, TComponent, Block, ?> componentType) {
        return (TComponent) components.get(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> Optional<TComponent> findComponent(ComponentType<BlockComponent, TComponent, Block, ?> componentType) {
        return ComponentHolder.super.findComponent(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> TComponent getComponentOrThrow(ComponentType<BlockComponent, TComponent, Block, ?> componentType) {
        return ComponentHolder.super.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends BlockComponent> void runForComponent(ComponentType<BlockComponent, TComponent, Block, ?> componentType, Consumer<TComponent> action) {
        ComponentHolder.super.runForComponent(componentType, action);
    }

    @Override
    public final boolean hasComponent(ComponentType<BlockComponent, ?, Block, ?> componentType) {
        return ComponentHolder.super.hasComponent(componentType);
    }

    @Override
    public final Set<ComponentType<BlockComponent, ?, Block, ?>> getComponentTypes() {
        return components.keySet();
    }

    @Override
    public final Collection<BlockComponent> getComponents() {
        return components.values();
    }

    @Override
    public final Block unwrap() {
        return this;
    }
    // endregion

    // region: Overrides
    @MustBeInvokedByOverriders
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return BlockComponentHelper.getStateForPlacement(this, context, defaultBlockState());
    }

    @MustBeInvokedByOverriders
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        BlockComponentHelper.setPlacedBy(this, level, pos, blockState, placer, stack);
        super.setPlacedBy(level, pos, blockState, placer, stack);
    }

    @MustBeInvokedByOverriders
    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        BlockComponentHelper.onPlace(this, blockState, level, pos, oldBlockState, movedByPiston);
        super.onPlace(blockState, level, pos, oldBlockState, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockComponentHelper.affectNeighborsAfterRemoval(this, blockState, level, pos, movedByPiston);
        super.affectNeighborsAfterRemoval(blockState, level, pos, movedByPiston);
    }

    @MustBeInvokedByOverriders
    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        var interactionResult = BlockComponentHelper.useItemOn(this, stack, blockState, level, pos, player, hand, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        return super.useItemOn(stack, blockState, level, pos, player, hand, result);
    }

    @MustBeInvokedByOverriders
    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        var interactionResult = BlockComponentHelper.useWithoutItem(this, blockState, level, pos, player, result);

        if(interactionResult.consumesAction())
            return interactionResult;

        if(openMenu(level, pos, blockState, player))
            return InteractionResult.SUCCESS;

        return super.useWithoutItem(blockState, level, pos, player, result);
    }

    @MustBeInvokedByOverriders
    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        var componentSignal = BlockComponentHelper.getAnalogOutputSignal(this, blockState, level, pos);
        return componentSignal + super.getAnalogOutputSignal(blockState, level, pos);
    }

    @MustBeInvokedByOverriders
    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        var hasSignal = BlockComponentHelper.hasAnalogOutputSignal(this, blockState);
        return hasSignal || super.hasAnalogOutputSignal(blockState);
    }

    @MustBeInvokedByOverriders
    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathType) {
        var isPathfindable = BlockComponentHelper.isPathfindable(this, blockState, pathType);
        return super.hasAnalogOutputSignal(blockState) && isPathfindable;
    }

    @MustBeInvokedByOverriders
    @Override
    protected void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockComponentHelper.tick(this, blockState, level, pos, random);
        super.tick(blockState, level, pos, random);
    }

    @MustBeInvokedByOverriders
    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier) {
        BlockComponentHelper.entityInside(this, blockState, level, pos, entity, applier);
        super.entityInside(blockState, level, pos, entity, applier);
    }

    @MustBeInvokedByOverriders
    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        BlockComponentHelper.handlePrecipitation(this, blockState, level, pos, precipitation);
        super.handlePrecipitation(blockState, level, pos, precipitation);
    }

    @MustBeInvokedByOverriders
    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        var result = BlockComponentHelper.rotate(this, blockState, rotation);
        return super.rotate(result, rotation);
    }

    @MustBeInvokedByOverriders
    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        var result = BlockComponentHelper.mirror(this, blockState, mirror);
        return super.mirror(result, mirror);
    }

    @MustBeInvokedByOverriders
    @Override
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState blockState, EntityType<?> entityType, LevelReader level, BlockPos pos, float orientation) {
        return BlockComponentHelper.getRespawnPosition(this, blockState, entityType, level, pos, orientation).or(() -> super.getRespawnPosition(blockState, entityType, level, pos, orientation));
    }
    // endregion
}
