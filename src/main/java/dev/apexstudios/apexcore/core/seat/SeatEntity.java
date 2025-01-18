package dev.apexstudios.apexcore.core.seat;

import com.google.common.base.Predicates;
import dev.apexstudios.apexcore.lib.seat.Seat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SeatEntity extends Entity {
    public SeatEntity(EntityType<? extends SeatEntity> entityType, Level level) {
        super(entityType, level);

        blocksBuilding = false;
    }

    @Override
    public void tick() {
        super.tick();

        var blockState = getInBlockState();

        if(!Seat.isOccupied(blockState) || getPassengers().size() != 1)
            discard();
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        Seat.setOccupied(level(), blockPosition(), false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float v) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return super.canAddPassenger(passenger) && Seat.maySit(passenger);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        passenger.setPose(Pose.SITTING);
        Seat.notifyCapabilityListeners(passenger, blockPosition(), getInBlockState(), true);
        super.addPassenger(passenger);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        passenger.setPose(Pose.STANDING);
        Seat.notifyCapabilityListeners(passenger, blockPosition(), getInBlockState(), false);
        super.removePassenger(passenger);
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        var shape = getInBlockState().getCollisionShape(level(), blockPosition()).move(Vec3.atCenterOf(blockPosition()));
        var pos = super.getPassengerRidingPosition(entity);
        return new Vec3(pos.x(), Math.min(pos.y(), shape.min(Direction.Axis.Y)), pos.z());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        var dismountLocation = DismountHelper.findSafeDismountLocation(getType(), level(), blockPosition(), true);
        return dismountLocation == null ? super.getDismountLocationForPassenger(passenger) : dismountLocation;
    }

    public static boolean sit(Level level, BlockPos pos, LivingEntity sitter) {
        var blockState = level.getBlockState(pos);

        if(Seat.isOccupied(blockState))
            return false;

        if(!level.isClientSide) {
            var entity = SeatSetup.ENTITY.value().create(level, EntitySpawnReason.SPAWN_ITEM_USE);

            if(entity == null)
                return false;

            entity.moveTo(pos, 0F, 0F);

            var leashed = level.getEntities(
                    EntityTypeTest.forClass(LivingEntity.class),
                    sitter.getBoundingBox().inflate(8D),
                    ent -> ent instanceof Leashable leashable && leashable.getLeashHolder() == sitter
            );

            for(var toSit : leashed) {
                if(toSit.startRiding(entity)) {
                    Seat.setOccupied(level, pos, true);
                    level.addFreshEntity(entity);
                    return true;
                }
            }

            if(sitter.startRiding(entity)) {
                Seat.setOccupied(level, pos, true);
                level.addFreshEntity(entity);
                return true;
            }

            return false;
        }

        return true;
    }

    public static boolean unsit(Level level, BlockPos pos) {
        var blockState = level.getBlockState(pos);

        if(!Seat.isOccupied(blockState))
            return false;

        if(!level.isClientSide) {
            var seats = level.getEntities(SeatSetup.ENTITY.value(), new AABB(pos).inflate(.5D), Predicates.alwaysTrue());

            for(var entity : seats) {
                if(!entity.getPassengers().isEmpty()) {
                    entity.ejectPassengers();
                    return true;
                }
            }

            return false;
        }

        return true;
    }
}
