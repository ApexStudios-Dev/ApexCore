package dev.apexstudios.apexcore.lib.seat;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.core.seat.SeatEntity;
import dev.apexstudios.apexcore.core.seat.SeatSetup;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;

public interface Seat {
    BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    EntityCapability<Runnable, @NotNull CapabilityContext> SEATED_CAPABILITY = EntityCapability.create(ApexCore.identifier("seated"), Runnable.class, CapabilityContext.class);
    EntityCapability<Runnable, @NotNull CapabilityContext> UNSEATED_CAPABILITY = EntityCapability.create(ApexCore.identifier("unseated"), Runnable.class, CapabilityContext.class);

    static boolean isSeat(BlockState blockState) {
        var block = blockState.getBlock();
        return block instanceof Seat || BlockComponentHelper.hasComponent(blockState, BlockComponentTypes.SEAT);
    }

    static boolean isOccupied(BlockState blockState) {
        return isSeat(blockState) ? blockState.getValue(OCCUPIED) : false;
    }

    static BlockState setOccupiedState(BlockState blockState, boolean occupied) {
        return isSeat(blockState) ? blockState.setValue(OCCUPIED, occupied) : blockState;
    }

    static void setOccupied(LevelWriter level, BlockPos pos, BlockState blockState, boolean occupied) {
        var newBlockState = setOccupiedState(blockState, occupied);

        if(newBlockState != blockState)
            level.setBlock(pos, newBlockState, Block.UPDATE_ALL);

        BlockComponentHelper.runForComponent(newBlockState, BlockComponentTypes.MULTI_BLOCK, multiBlock -> {
            var index = multiBlock.indexOf(newBlockState);

            if(newBlockState.is(SeatSetup.ORIGIN_ONLY)) {
                var origin = multiBlock.getOrigin(pos, newBlockState);

                for(var i = 0; i < multiBlock.size(); i++) {
                    if(i != index) {
                        var otherBlockState = multiBlock.withIndex(newBlockState, i);
                        level.setBlock(multiBlock.getPos(origin, otherBlockState), setOccupiedState(otherBlockState, occupied), Block.UPDATE_ALL);
                    }
                }
            }
        });
    }

    static void setOccupied(Level level, BlockPos pos, boolean occupied) {
        setOccupied(level, pos, level.getBlockState(pos), occupied);
    }

    static InteractionResult useSeat(Level level, BlockPos pos, LivingEntity sitter) {
        var result = trySit(level, pos, sitter);

        if(!result.consumesAction())
            result = tryUnsit(level, pos);

        return result;
    }

    static InteractionResult trySit(Level level, BlockPos pos, LivingEntity sitter) {
        return SeatEntity.sit(level, pos, sitter) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    static InteractionResult tryUnsit(Level level, BlockPos pos) {
        return SeatEntity.unsit(level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    static boolean maySit(EntityType<?> entityType) {
        return !entityType.is(SeatSetup.BLACKLIST);
    }

    static boolean maySit(Entity entity) {
        return maySit(entity.getType()) && entity instanceof LivingEntity;
    }

    static void notifyCapabilityListeners(Entity entity, BlockPos pos, BlockState blockState, boolean seated) {
        var capability = entity.getCapability(seated ? SEATED_CAPABILITY : UNSEATED_CAPABILITY, new Seat.CapabilityContext(pos, blockState));

        if(capability != null)
            capability.run();
    }

    static <TEntity extends Entity> void registerCapabilities(RegisterCapabilitiesEvent event, EntityType<TEntity> entityType, TriConsumer<TEntity, BlockPos, BlockState> seated, TriConsumer<TEntity, BlockPos, BlockState> unseated) {
        event.registerEntity(SEATED_CAPABILITY, entityType, (entity, context) -> () -> seated.accept(entity, context.pos(), context.blockState()));
        event.registerEntity(UNSEATED_CAPABILITY, entityType, (entity, context) -> () -> unseated.accept(entity, context.pos(), context.blockState()));
    }

    @SafeVarargs
    static <TEntity extends Entity> void registerCapabilities(RegisterCapabilitiesEvent event, TriConsumer<TEntity, BlockPos, BlockState> seated, TriConsumer<TEntity, BlockPos, BlockState> unseated, EntityType<? extends TEntity>... entityTypes) {
        for(var entityType : entityTypes) {
            registerCapabilities(event, entityType, seated::accept, unseated::accept);
        }
    }

    record CapabilityContext(BlockPos pos, BlockState blockState) { }
}
