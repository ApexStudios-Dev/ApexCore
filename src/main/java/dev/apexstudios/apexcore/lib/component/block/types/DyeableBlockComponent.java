package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class DyeableBlockComponent extends BaseBlockComponent {
    public static final ComponentType<BlockComponent, DyeableBlockComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("dyeable"),
            Builder::new,
            DyeableBlockComponent::new
    );

    private final EnumProperty<DyeColor> property;
    private final DyeColor defaultColor;

    private DyeableBlockComponent(ComponentHolder<BlockComponent> holder, Builder builder) {
        super(holder);

        builder.allow(builder.defaultColor);
        property = EnumProperty.create("color", DyeColor.class, builder.colors.toArray(DyeColor[]::new));
        defaultColor = builder.defaultColor;
    }

    public Property<DyeColor> property() {
        return property;
    }

    public DyeColor defaultColor() {
        return defaultColor;
    }

    public DyeColor get(BlockState blockState) {
        return blockState.getValue(property);
    }

    public BlockState set(BlockState blockState, DyeColor color) {
        return blockState.setValue(property, color);
    }

    public boolean isAllowed(@Nullable DyeColor color) {
        return color != null && property.getPossibleValues().contains(color);
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return set(blockState, defaultColor);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(property);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        var color = getColorForPlacement(context, this);
        return set(blockState, color);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(!level.isClientSide) {
            var color = DyeColor.getColor(stack);
            var currentColor = get(blockState);

            if(color != currentColor && isAllowed(color)) {
                level.setBlock(pos, set(blockState, color), Block.UPDATE_ALL);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.useItemOn(stack, blockState, level, pos, player, hand, result);
    }

    @Override
    public void modifyCloneItemStack(ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData) {
        if(includeData)
            stack.set(DataComponents.BASE_COLOR, get(blockState));
    }

    @Override
    public void modifyCloneItemStack(ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        if(player.isCreative() || includeData)
            stack.set(DataComponents.BASE_COLOR, get(blockState));
    }

    @Nullable
    public static DyeColor getColorForPlacement(BlockPlaceContext context, Predicate<@Nullable DyeColor> validColor) {
        var player = context.getPlayer();

        if(player != null) {
            var otherHand = switch (context.getHand()) {
                case OFF_HAND -> InteractionHand.MAIN_HAND;
                case MAIN_HAND -> InteractionHand.OFF_HAND;
            };

            var dyeColor = DyeColor.getColor(player.getItemInHand(otherHand));

            if(validColor.test(dyeColor))
                return dyeColor;
        }

        var color = context.getItemInHand().get(DataComponents.BASE_COLOR);
        return validColor.test(color) ? color : null;
    }

    public static DyeColor getColorForPlacement(BlockPlaceContext context, DyeableBlockComponent component) {
        var result = getColorForPlacement(context, component::isAllowed);
        return result == null ? component.defaultColor : result;
    }

    public static final class Builder implements ComponentBuilder {
        private DyeColor defaultColor = DyeColor.WHITE;
        private final Set<DyeColor> colors = EnumSet.allOf(DyeColor.class);

        public Builder defaultColor(DyeColor defaultColor) {
            this.defaultColor = defaultColor;
            return this;
        }

        public Builder allow(DyeColor color) {
            colors.add(color);
            return this;
        }

        public Builder allow(DyeColor... colors) {
            Collections.addAll(this.colors, colors);
            return this;
        }

        public Builder disallow(DyeColor color) {
            colors.remove(color);
            return this;
        }

        public Builder disallow(DyeColor... colors) {
            ApexUtil.removeAll(this.colors, colors);
            return this;
        }
    }
}
