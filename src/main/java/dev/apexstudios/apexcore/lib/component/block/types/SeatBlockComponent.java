package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.core.seat.SeatEntity;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import dev.apexstudios.apexcore.lib.util.ApexTags;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;

public final class SeatBlockComponent extends BaseBlockComponent {
    public static final ComponentType<BlockComponent, SeatBlockComponent, Block, ComponentBuilder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("seat"),
            SeatBlockComponent::new
    );

    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public static final EntityCapability<Runnable, @NotNull CapabilityContext> SEATED_CAPABILITY = EntityCapability.create(ApexCore.identifier("seated"), Runnable.class, CapabilityContext.class);
    public static final EntityCapability<Runnable, @NotNull CapabilityContext> UNSEATED_CAPABILITY = EntityCapability.create(ApexCore.identifier("unseated"), Runnable.class, CapabilityContext.class);
    public static final EntityCapability<BooleanSupplier, @NotNull CapabilityContext> MAY_SIT_CAPABILITY = EntityCapability.create(ApexCore.identifier("may_sit"), BooleanSupplier.class, CapabilityContext.class);

    private SeatBlockComponent(ComponentHolder<BlockComponent, Block> holder) {
        super(holder);
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState.setValue(OCCUPIED, false);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(OCCUPIED);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var result = trySit(level, pos, player);

        if(!result.consumesAction())
            result = tryUnsit(level, pos);

        return result;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        if(!(entity instanceof Player) && entity instanceof LivingEntity living)
            trySit(level, pos, living);
    }

    private InteractionResult trySit(Level level, BlockPos pos, LivingEntity sitter) {
        return SeatEntity.sit(level, pos, sitter) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private InteractionResult tryUnsit(Level level, BlockPos pos) {
        return SeatEntity.unsit(level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public static boolean isSeat(BlockState blockState) {
        return BlockComponentHelper.hasComponent(blockState, COMPONENT_TYPE);
    }

    public static boolean isOccupied(BlockState blockState) {
        return isSeat(blockState) ? blockState.getValue(OCCUPIED) : false;
    }

    public static void setOccupied(LevelWriter level, BlockPos pos, BlockState blockState, boolean occupied) {
        if(!isSeat(blockState))
            return;

        var newBlockState = blockState.setValue(OCCUPIED, occupied);

        if(newBlockState != blockState)
            level.setBlock(pos, newBlockState, Block.UPDATE_ALL);

        BlockComponentHelper.runForComponent(newBlockState, BlockComponentTypes.MULTI_BLOCK, multiBlock -> {
            var index = multiBlock.indexOf(newBlockState);

            if(newBlockState.is(ApexTags.Blocks.SEAT_ORIGIN_ONLY)) {
                var origin = multiBlock.getOrigin(pos, newBlockState);

                for(var i = 0; i < multiBlock.size(); i++) {
                    if(i != index) {
                        var otherBlockState = multiBlock.withIndex(newBlockState, i);
                        level.setBlock(multiBlock.getPos(origin, otherBlockState), otherBlockState.setValue(OCCUPIED, occupied), Block.UPDATE_ALL);
                    }
                }
            }
        });
    }

    public static void setOccupied(Level level, BlockPos pos, boolean occupied) {
        setOccupied(level, pos, level.getBlockState(pos), occupied);
    }

    public static boolean maySit(EntityType<?> entityType) {
        return !entityType.is(ApexTags.EntityTypes.SEAT_BLACKLIST);
    }

    public static boolean maySit(Entity entity) {
        if(!maySit(entity.getType()))
            return false;
        if(entity.hasControllingPassenger())
            return false;
        if(!(entity instanceof LivingEntity))
            return false;

        var maySit = entity.getCapability(MAY_SIT_CAPABILITY, new CapabilityContext(entity.getOnPos(), entity.getBlockStateOn()));
        return maySit == null || maySit.getAsBoolean();
    }

    public static void notifyCapabilityListeners(Entity entity, BlockPos pos, BlockState blockState, boolean seated) {
        var capability = entity.getCapability(seated ? SEATED_CAPABILITY : UNSEATED_CAPABILITY, new CapabilityContext(pos, blockState));

        if(capability != null)
            capability.run();
    }

    public static <TEntity extends Entity> void registerCapabilities(RegisterCapabilitiesEvent event, EntityType<TEntity> entityType, TriConsumer<TEntity, BlockPos, BlockState> seated, TriConsumer<TEntity, BlockPos, BlockState> unseated) {
        event.registerEntity(SEATED_CAPABILITY, entityType, (entity, context) -> () -> seated.accept(entity, context.pos(), context.blockState()));
        event.registerEntity(UNSEATED_CAPABILITY, entityType, (entity, context) -> () -> unseated.accept(entity, context.pos(), context.blockState()));
    }

    @SafeVarargs
    public static <TEntity extends Entity> void registerCapabilities(RegisterCapabilitiesEvent event, TriConsumer<TEntity, BlockPos, BlockState> seated, TriConsumer<TEntity, BlockPos, BlockState> unseated, EntityType<? extends TEntity>... entityTypes) {
        for(var entityType : entityTypes) {
            registerCapabilities(event, entityType, seated::accept, unseated::accept);
        }
    }

    public record CapabilityContext(BlockPos pos, BlockState blockState) { }
}
