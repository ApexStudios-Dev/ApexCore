package dev.apexstudios.apexcore.api;

import dev.apexstudios.apexcore.extensions.FluidTypeExtensions;
import dev.apexstudios.apexcore.extensions.ItemExtensions;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoveSimulationType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.joml.Math;
import org.jspecify.annotations.Nullable;

public final class FakeItemEntity extends Entity implements TraceableEntity {
    public static final String TAG_AGE = "age";
    public static final String TAG_ITEM = "item";
    public static final String TAG_LIFETIME = "lifetime";
    public static final String TAG_OWNER = "owner";
    public static final String TAG_THROWER = "thrower";
    public static final String TAG_MERGABLE = "mergable";

    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(FakeItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_MERGABLE = SynchedEntityData.defineId(FakeItemEntity.class, EntityDataSerializers.BOOLEAN);

    private int age = ItemEntity.DEFAULT_AGE;
    private @Nullable EntityReference<Entity> thrower;
    private @Nullable UUID target;
    public final float bobOffset = random.nextFloat() * Math.PI_TIMES_2_f;
    private int lifetime = ItemEntity.LIFETIME;

    FakeItemEntity(EntityType<?> type, Level level) {
        super(type, level);

        setYRot(random.nextFloat() * 360F);
    }

    public FakeItemEntity(Level level, ItemStack stack) {
        this(ApexCore.FAKE_ITEM.value(), level);

        setItem(stack.copy());
    }

    public FakeItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(level, stack);

        setPos(x, y, z);
        setDeltaMovement(random.nextDouble() * .2D - .1D, .2D, random.nextDouble() * .2D - .1D);
    }

    @Override
    public boolean dampensVibrations() {
        return getItem().is(ItemTags.DAMPENS_VIBRATIONS);
    }

    @Override
    public @Nullable Entity getOwner() {
        return EntityReference.getEntity(thrower, level());
    }

    @Override
    public void restoreFrom(Entity oldEntity) {
        super.restoreFrom(oldEntity);

        if(oldEntity instanceof FakeItemEntity item) {
            thrower = item.thrower;
        }
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_ITEM, ItemStack.EMPTY);
        entityData.define(DATA_MERGABLE, true);
    }

    @Override
    protected double getDefaultGravity() {
        return .04F; // ItemEntity
    }

    @Override
    public void tick() {
        if(getItem().getItem() instanceof ItemExtensions extension && extension.onEntityItemUpdate(getItem(), this)) {
            return;
        }

        if(getItem().isEmpty()) {
            discard();
            return;
        }

        super.tick();

        xo = getX();
        yo = getY();
        zo = getZ();

        var oldMovement = getDeltaMovement();
        var fluidType = getMaxHeightFluidType();
        var applyVanillaMovement = true;

        if(!fluidType.isAir() && !fluidType.isVanilla() && getFluidTypeHeight(fluidType) > ItemEntity.FLOAT_HEIGHT) {
            if(fluidType instanceof FluidTypeExtensions extension) {
                extension.setItemMovement(this);
                applyVanillaMovement = false;
            }
        }

        if(applyVanillaMovement) {
            if(isInWater() && getFluidHeight(FluidTags.WATER) > ItemEntity.FLOAT_HEIGHT) {
                setUnderwaterMovement();
            } else if(isInLava() && getFluidHeight(FluidTags.LAVA) > ItemEntity.FLOAT_HEIGHT) {
                setUnderLavaMovement();
            } else {
                applyGravity();
            }
        }

        if(level().isClientSide()) {
            noPhysics = false;
        } else {
            noPhysics = !level().noCollision(this, getBoundingBox().deflate(AABB.EPSILON));

            if(noPhysics) {
                moveTowardsClosestSpace(getX(), (getBoundingBox().minY - getBoundingBox().maxY) / 2D, getZ());
            }
        }

        if(onGround() && !(getDeltaMovement().horizontalDistanceSqr() > Mth.EPSILON) && ((tickCount + getId()) & 4) != 0) {
            applyEffectsFromBlocksForLastMovements();
        } else {
            move(MoverType.SELF, getDeltaMovement());
            applyEffectsFromBlocks();

            var airDrag = getAirDrag();
            var groundFriction = airDrag;

            if(onGround()) {
                var groundPos = getBlockPosBelowThatAffectsMyMovement();
                groundFriction *= level().getBlockState(groundPos).getFriction(level(), groundPos, this);
            }

            setDeltaMovement(getDeltaMovement().multiply(groundFriction, airDrag, groundFriction));

            if(onGround()) {
                var movement = getDeltaMovement();

                if(movement.y() < 0D) {
                    setDeltaMovement(movement.multiply(1D, -.5D, 1D));
                }
            }
        }

        var moved = Mth.floor(xo) != Mth.floor(getX()) ||
                Mth.floor(yo) != Mth.floor(getY()) ||
                Mth.floor(zo) != Mth.floor(getZ());
        var rate = moved ? 2 : 40;

        if(tickCount % rate == 0 && !level().isClientSide() && isMergable()) {
            mergeWithNeighbours();
        }

        age++;
        needsSync = needsSync | updateFluidInteraction();

        if(!level().isClientSide()) {
            var value = getDeltaMovement().subtract(oldMovement).lengthSqr();

            if(value > .01D) {
                needsSync = true;
            }
        }

        if(!level().isClientSide() && age >= lifetime) {
            discard();
        }

        if(getItem().isEmpty() && !isRemoved()) {
            discard();
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return getOnPos(.999999F);
    }

    private void setUnderwaterMovement() {
        setFluidMovement(.99F);
    }

    private void setUnderLavaMovement() {
        setFluidMovement(.95F);
    }

    public void setFluidMovement(double multiplier) {
        var movement = getDeltaMovement();
        setDeltaMovement(movement.x() * multiplier, movement.y() + (movement.y() < .06F ? 5.0E-4F : 0F), movement.z() * multiplier);
    }

    private void mergeWithNeighbours() {
        if(!isMergable()) {
            return;
        }

        var entities = level().getEntitiesOfClass(
                FakeItemEntity.class,
                getBoundingBox().inflate(.5D, 0D, .5D),
                this::isMergable
        );

        for(var other : entities) {
            var thisStack = getItem();
            var otherStack = other.getItem();

            if(otherStack.count() < thisStack.count()) {
                merge(this, thisStack, other, otherStack);
            } else {
                merge(other, otherStack, this, thisStack);
            }

            if(isRemoved()) {
                break;
            }
        }
    }

    private boolean isMergingAllowed() {
        return entityData.get(DATA_MERGABLE);
    }

    private boolean isMergable() {
        if (!isAlive()) {
            return false;
        }

        if(!isMergingAllowed()) {
            return false;
        }

        var item = getItem();
        return item.count() < item.getMaxStackSize();
    }

    private boolean isMergable(FakeItemEntity other) {
        if(other == this) {
            return false;
        }

        if(!other.isMergable()) {
            return false;
        }

        if(!Objects.equals(target, other.target)) {
            return false;
        }

        if(!ItemEntity.areMergable(getItem(), other.getItem())) {
            return false;
        }

        if(lifetime != other.lifetime) {
            return false;
        }

        return true;
    }

    public void setMergable(boolean mergable) {
        entityData.set(DATA_MERGABLE, mergable);
    }

    @Override
    public boolean fireImmune() {
        return !getItem().canBeHurtBy(damageSources().inFire()) || super.fireImmune();
    }

    @Override
    protected boolean shouldPlayLavaHurtSound() {
        return tickCount % 10 == 0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        if(explosion.shouldAffectBlocklikeEntities()) {
            return super.ignoreExplosion(explosion);
        }

        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt(TAG_AGE, age);
        output.putInt(TAG_LIFETIME, lifetime);
        output.putBoolean(TAG_MERGABLE, isMergingAllowed());
        output.storeNullable(TAG_OWNER, UUIDUtil.CODEC, target);

        EntityReference.store(thrower, output, TAG_THROWER);

        if(!getItem().isEmpty()) {
            output.store(TAG_ITEM, ItemStack.CODEC, getItem());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        age = input.getIntOr(TAG_AGE, ItemEntity.DEFAULT_AGE);
        lifetime = input.getIntOr(TAG_LIFETIME, ItemEntity.LIFETIME);
        target = input.read(TAG_OWNER, UUIDUtil.CODEC).orElse(null);
        thrower = EntityReference.read(input, TAG_THROWER);

        setMergable(input.getBooleanOr(TAG_MERGABLE, true));
        setItem(input.read(TAG_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY));

        if(getItem().isEmpty()) {
            discard();
        }
    }

    @Override
    public Component getName() {
        var name = getCustomName();
        return name == null ? getItem().getItemName() : name;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public @Nullable Entity teleport(TeleportTransition transition) {
        var entity = super.teleport(transition);

        if(!level().isClientSide() && entity instanceof FakeItemEntity item) {
            item.mergeWithNeighbours();
        }

        return entity;
    }

    public ItemStack getItem() {
        return getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack stack) {
        getEntityData().set(DATA_ITEM, stack);
    }

    public void setTarget(@Nullable UUID target) {
        this.target = target;
    }

    public @Nullable UUID getTarget() {
        return target;
    }

    public void setThrower(Entity thrower) {
        this.thrower = EntityReference.of(thrower);
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 180F - ItemEntity.getSpin(age + .5F, bobOffset) / Math.PI_TIMES_2_f * 360F;
    }

    @Override
    public @Nullable SlotAccess getSlot(int slot) {
        return slot == 0 ? SlotAccess.of(this::getItem, this::setItem) : super.getSlot(slot);
    }

    @Override
    public MoveSimulationType getMoveSimulationType() {
        return MoveSimulationType.SERVER_AND_CLIENT;
    }

    public static @Nullable FakeItemEntity drop(LivingEntity thrower, ItemStack stack, boolean randomly, boolean thrownFromHand) {
        if(stack.isEmpty()) {
            return null;
        }

        var random = thrower.getRandom();
        var yHandPos = thrower.getEyeY() - .3F;
        var entity = new FakeItemEntity(thrower.level(), thrower.getX(), yHandPos, thrower.getZ(), stack);

        if(thrownFromHand) {
            entity.setThrower(thrower);
        }

        if(randomly) {
            var pow = random.nextFloat() * .5F;
            var dir = random.nextFloat() * Math.PI_TIMES_2_f;

            entity.setDeltaMovement(
                    -Mth.sin(dir) * pow,
                    .2F,
                    Mth.cos(dir) * pow
            );
        } else {
            var pow = .3F;
            var rotX = thrower.getXRot() * (Math.PI / 180F);
            var rotY = thrower.getYRot() * (Math.PI / 180F);
            var sinX = Mth.sin(rotX);
            var cosX = Mth.cos(rotX);
            var sinY = Mth.sin(rotY);
            var cosY = Mth.cos(rotY);
            var dir = random.nextFloat() * Math.PI_TIMES_2_f;
            var pow2 = .02F * random.nextFloat();

            entity.setDeltaMovement(
                    -sinY * cosX * pow + Math.cos(dir) * pow2,
                    -sinX * pow + .1F + (random.nextFloat() - random.nextFloat()) * .1F,
                    cosY * cosX * pow + Math.sin(dir) * pow2
            );
        }

        return entity;
    }

    private static void merge(FakeItemEntity toItem, ItemStack toStack, ItemStack fromStack) {
        var newToStack = ItemEntity.merge(toStack, fromStack, Item.DEFAULT_MAX_STACK_SIZE);
        toItem.setItem(newToStack);
    }

    private static void merge(FakeItemEntity toItem, ItemStack toStack, FakeItemEntity fromItem, ItemStack fromStack) {
        merge(toItem, toStack, fromStack);

        toItem.age = Math.min(toItem.age, fromItem.age);

        if(fromStack.isEmpty()) {
            fromItem.discard();
        }
    }
}
