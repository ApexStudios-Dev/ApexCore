package dev.apexstudios.apexcore.lib.multiblock;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public final class MultiBlockComponent extends BaseBlockComponent implements MultiBlock {
    public static final ComponentType<BlockComponent, MultiBlockComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("multi_block"),
            Builder::new,
            MultiBlockComponent::new
    );

    private final MultiBlockType multiBlockType;

    private MultiBlockComponent(ComponentHolder<BlockComponent> holder, Builder builder) {
        super(holder);

        multiBlockType = Objects.requireNonNull(builder.multiBlockType.get());
    }

    @Override
    public MultiBlockType getMultiBlockType() {
        return multiBlockType;
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState.setValue(multiBlockType.property(), ORIGIN_INDEX);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(multiBlockType.property());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        if(!multiBlockType.canPlaceAt(context.getLevel(), context.getClickedPos(), blockState, context.getPlayer()))
            return null;

        return blockState;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        multiBlockType.placeMultiBlocks(level, pos, blockState);
    }

    /*@Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        if(!oldBlockState.is(blockState.getBlock()))
            multiBlockType.placeMultiBlocks(level, pos, blockState);
    }*/

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean movedByPiston) {
        if(!newBlockState.is(blockState.getBlock()))
            multiBlockType.removeMultiBlocks(level, pos, blockState, null);
    }

    public static final class Builder implements ComponentBuilder {
        private Supplier<@Nullable MultiBlockType> multiBlockType = () -> null;

        public Builder type(MultiBlockType multiBlockType) {
            this.multiBlockType = () -> multiBlockType;
            return this;
        }

        public Builder type(UnaryOperator<MultiBlockType.Builder> builder) {
            multiBlockType = () -> builder.apply(MultiBlockType.builder()).build();
            return this;
        }
    }
}
