package dev.apexstudios.apexcore.api.block;

import dev.apexstudios.apexcore.api.util.ApexTags;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.common.seat.SeatEntity;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.commons.lang3.function.TriConsumer;

public interface Seat {
    EntityCapability<Runnable, CapabilityContext> SEATED_CAPABILITY = EntityCapability.create(ApexCore.identifier("seated"), Runnable.class, CapabilityContext.class);
    EntityCapability<Runnable, CapabilityContext> UNSEATED_CAPABILITY = EntityCapability.create(ApexCore.identifier("unseated"), Runnable.class, CapabilityContext.class);
    EntityCapability<BooleanSupplier, CapabilityContext> MAY_SIT_CAPABILITY = EntityCapability.create(ApexCore.identifier("may_sit"), BooleanSupplier.class, CapabilityContext.class);

    BooleanProperty DEFAULT_PROPERTY = BlockStateProperties.OCCUPIED;

    default Vec3 getSeatPosition(BlockGetter level, BlockPos pos, BlockState blockState) {
        return Vec3.atCenterOf(pos);
    }

    default boolean isSeatOccupied(BlockState blockState) {
        return blockState.getValueOrElse(DEFAULT_PROPERTY, false);
    }

    default void setSeatOccupied(Level level, BlockPos pos, BlockState blockState, boolean occupied) {
        level.setBlockAndUpdate(pos, blockState.trySetValue(DEFAULT_PROPERTY, occupied));
    }

    static Vec3 getPosition(BlockGetter level, BlockPos pos, BlockState blockState) {
        if(blockState.getBlock() instanceof Seat seat)
            return seat.getSeatPosition(level, pos, blockState);

        return Vec3.atCenterOf(pos);
    }

    static boolean isOccupied(BlockState blockState) {
        if(blockState.getBlock() instanceof Seat seat)
            return seat.isSeatOccupied(blockState);

        return blockState.getValueOrElse(DEFAULT_PROPERTY, false);
    }

    static void setOccupied(Level level, BlockPos pos, BlockState blockState, boolean occupied) {
        if(blockState.getBlock() instanceof Seat seat)
            seat.setSeatOccupied(level, pos, blockState, occupied);
        else
            level.setBlockAndUpdate(pos, blockState.trySetValue(DEFAULT_PROPERTY, occupied));
    }

    static void setOccupied(Level level, BlockPos pos, boolean occupied) {
        setOccupied(level, pos, level.getBlockState(pos), occupied);
    }

    static boolean maySit(EntityType<?> entityType) {
        return !entityType.builtInRegistryHolder().is(ApexTags.EntityTypes.SEAT_BLACKLIST);
    }

    static boolean maySit(Entity entity) {
        if(!maySit(entity.getType()))
            return false;
        if(entity.hasControllingPassenger())
            return false;
        if(!(entity instanceof LivingEntity))
            return false;

        var maySit = entity.getCapability(MAY_SIT_CAPABILITY, new CapabilityContext(entity.getOnPos(), entity.getBlockStateOn()));
        return maySit == null || maySit.getAsBoolean();
    }

    static void notifyCapabilityListeners(Entity entity, BlockPos pos, BlockState blockState, boolean seated) {
        var capability = entity.getCapability(seated ? SEATED_CAPABILITY : UNSEATED_CAPABILITY, new CapabilityContext(pos, blockState));

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

    static InteractionResult interactWith(Level level, BlockPos pos, LivingEntity sitter) {
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

    record CapabilityContext(BlockPos pos, BlockState blockState) { }
}
