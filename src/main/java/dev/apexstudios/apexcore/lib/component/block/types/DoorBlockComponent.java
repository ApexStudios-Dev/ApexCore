package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Requires FacingComponent
public final class DoorBlockComponent extends BaseBlockComponent {
    public static final ComponentType<BlockComponent, DoorBlockComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("door"),
            Builder::new,
            DoorBlockComponent::new
    );

    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private final boolean openByHand;
    private final boolean openWithWindCharge;
    private final Boolean2ObjectFunction<SoundEvent> soundGetter;

    private DoorBlockComponent(ComponentHolder<BlockComponent> holder, Builder builder) {
        super(holder);

        openByHand = builder.openByhand;
        openWithWindCharge = builder.openWithWindCharge;
        soundGetter = builder.soundGetter;
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState.setValue(HINGE, DoorHingeSide.LEFT).setValue(POWERED, false).setValue(OPEN, false);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(HINGE);
        consumer.accept(POWERED);
        consumer.accept(OPEN);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, PathComputationType pathType) {
        return switch (pathType) {
            case LAND, AIR -> blockState.getValue(OPEN);
            default -> false;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        var facing = getComponentOrThrow(BlockComponentTypes.FACING).get(blockState);
        var hasPower = hasPower(context.getLevel(), context.getClickedPos(), blockState);
        return blockState.setValue(HINGE, getHinge(context, facing)).setValue(POWERED, hasPower).setValue(OPEN, hasPower);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if(!openByHand)
            return InteractionResult.PASS;

        setOpen(player, level, pos, blockState, !blockState.getValue(OPEN), false);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        var hasPower = hasPower(level, pos, blockState);
        setOpen(null, level, pos, blockState, hasPower, true);
    }

    @Override
    public void onExplosionHit(BlockState blockState, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropsConsumer) {
        if(explosion.canTriggerBlocks() && openWithWindCharge)
            setOpen(null, level, pos, blockState, !blockState.getValue(OPEN), false);
    }

    public void setOpen(@Nullable Entity source, Level level, BlockPos pos, BlockState blockState, boolean open, boolean setPowered) {
        if(setPowered) {
            if(blockState.getValue(POWERED) == open)
                return;
        } else {
            if(blockState.getValue(OPEN) == open)
                return;
        }

        var newBlockState = blockState.setValue(OPEN, open);

        if(setPowered)
            newBlockState = newBlockState.setValue(POWERED, open);

        level.setBlock(pos, newBlockState, Block.UPDATE_ALL);
        level.playSound(source, pos, open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1F, level.random.nextFloat() * .1F + .9F);
        level.gameEvent(source, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

        var multiBlock = getComponent(BlockComponentTypes.MULTI_BLOCK);

        if(multiBlock != null) {
            var multiBlockType = multiBlock.getMultiBlockType();
            var index = multiBlockType.indexOf(newBlockState);
            var origin = multiBlockType.getOrigin(pos, newBlockState);

            for(var i = 0; i < multiBlockType.size(); i++) {
                if(i == index)
                    continue;

                var otherBlockState = multiBlockType.withIndex(newBlockState, i);
                var otherPos = multiBlockType.getPos(origin, otherBlockState);
                var newOtherBlockState = otherBlockState.setValue(OPEN, open);

                if(setPowered)
                    newOtherBlockState = newOtherBlockState.setValue(POWERED, open);

                level.setBlock(otherPos, newOtherBlockState, Block.UPDATE_ALL);
            }
        }
    }

    private boolean hasPower(SignalGetter level, BlockPos pos, BlockState blockState) {
        var multiBlock = getComponent(BlockComponentTypes.MULTI_BLOCK);

        if(multiBlock != null) {
            var multiBlockType = multiBlock.getMultiBlockType();
            var index = multiBlockType.indexOf(blockState);
            var origin = multiBlockType.getOrigin(pos, blockState);

            for(var i = 0; i < multiBlockType.size(); i++) {
                if(i == index)
                    continue;

                var otherBlockState = multiBlockType.withIndex(blockState, i);
                var otherPos = multiBlockType.getPos(origin, otherBlockState);

                if(level.hasNeighborSignal(otherPos))
                    return true;
            }
        }

        return level.hasNeighborSignal(pos);
    }

    public static DoorHingeSide getHinge(BlockPlaceContext context, Direction facing) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var facingCCW = facing.getCounterClockWise();
        var posCCW = pos.relative(facingCCW);
        var blockStateCCW = level.getBlockState(posCCW);
        var facingCW = facing.getClockWise();
        var posCW = pos.relative(facingCW);
        var blockStateCW = level.getBlockState(posCW);
        var i = (blockStateCCW.isCollisionShapeFullBlock(level, posCCW) ? -1 : 0) + (blockStateCW.isCollisionShapeFullBlock(level, posCW) ? 1 : 0);
        var isDoorCCW = hasDoorComponent(blockStateCCW);
        var isDoorCW = hasDoorComponent(blockStateCW);

        if((!isDoorCCW || isDoorCW) && i <= 0) {
            if((!isDoorCW || isDoorCCW) && i >= 0) {
                var stepX = facing.getStepX();
                var stepZ = facing.getStepZ();
                var clickLocation = context.getClickLocation();
                var clickX = clickLocation.x - (double) pos.getX();
                var clickZ = clickLocation.z - (double) pos.getZ();

                return (stepX >= 0D || clickZ >= .5D) &&
                        (stepX <= 0D || clickZ <= .5D) &&
                        (stepZ >= 0D || clickX <= .5D) &&
                        (stepZ <= 0D || clickX >= .5D) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            }

            return DoorHingeSide.LEFT;
        }

        return DoorHingeSide.RIGHT;
    }

    private static boolean hasDoorComponent(BlockState blockState) {
        return BlockComponentHelper.hasComponent(blockState, COMPONENT_TYPE);
    }

    public static final class Builder implements ComponentBuilder {
        private boolean openByhand = true;
        private boolean openWithWindCharge = true;
        private Boolean2ObjectFunction<SoundEvent> soundGetter = open -> open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE;

        public Builder openByHand(boolean openByHand) {
            this.openByhand = openByHand;
            return this;
        }

        public Builder openWithWindCharge(boolean openWithWindCharge) {
            this.openWithWindCharge = openWithWindCharge;
            return this;
        }

        public Builder sound(Boolean2ObjectFunction<SoundEvent> soundGetter) {
            this.soundGetter = soundGetter;
            return this;
        }

        public Builder sound(Supplier<SoundEvent> openSound, Supplier<SoundEvent> closeSound) {
            return sound(open -> open ? openSound.get() : closeSound.get());
        }

        public Builder from(BlockSetType blockSet) {
            return openByHand(blockSet.canOpenByHand())
                    .openWithWindCharge(blockSet.canOpenByWindCharge())
                    .sound(blockSet::doorOpen, blockSet::doorClose);
        }
    }
}
