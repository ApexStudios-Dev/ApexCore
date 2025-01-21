package dev.apexstudios.apexcore.lib.seat;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public final class SeatBlockComponent extends BaseBlockComponent implements Seat {
    public static final ComponentType<BlockComponent, SeatBlockComponent, ComponentBuilder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("seat"),
            SeatBlockComponent::new
    );

    private SeatBlockComponent(ComponentHolder<BlockComponent> holder) {
        super(holder);
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState.setValue(Seat.OCCUPIED, false);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(Seat.OCCUPIED);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return Seat.useSeat(level, pos, player);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        if(!(entity instanceof Player) && entity instanceof LivingEntity living)
            Seat.trySit(level, pos, living);
    }
}