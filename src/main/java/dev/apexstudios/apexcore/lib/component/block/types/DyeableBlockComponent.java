package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
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
        return getStateForPlacement(property, defaultColor, context, blockState);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        return useItemOn(property, stack, blockState, level, pos);
    }

    @Override
    public void modifyCloneItemStack(ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData) {
        modifyCloneItemStack(property, stack, blockState, includeData, null);
    }

    @Override
    public void modifyCloneItemStack(ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        modifyCloneItemStack(property, stack, blockState, includeData, player);
    }

    public static boolean isAllowed(Property<DyeColor> property, @Nullable DyeColor color) {
        return color != null && property.getPossibleValues().contains(color);
    }

    @Nullable
    public static DyeColor getColorForPlacement(Property<DyeColor> property, BlockPlaceContext context) {
        var player = context.getPlayer();

        if(player != null) {
            var otherHand = switch (context.getHand()) {
                case OFF_HAND -> InteractionHand.MAIN_HAND;
                case MAIN_HAND -> InteractionHand.OFF_HAND;
            };

            var dyeColor = DyeColor.getColor(player.getItemInHand(otherHand));

            if(isAllowed(property, dyeColor))
                return dyeColor;
        }

        var color = context.getItemInHand().get(DataComponents.BASE_COLOR);
        return isAllowed(property, color) ? color : null;
    }

    public static DyeColor getColorForPlacement(Property<DyeColor> property, DyeColor defaultColor, BlockPlaceContext context) {
        var result = getColorForPlacement(property, context);
        return result == null ? defaultColor : result;
    }

    public static BlockState getStateForPlacement(Property<DyeColor> property, DyeColor defaultColor, BlockPlaceContext context, BlockState blockState) {
        var color = getColorForPlacement(property, defaultColor, context);
        return blockState.setValue(property, color);
    }

    public static InteractionResult useItemOn(Property<DyeColor> property, ItemStack stack, BlockState blockState, Level level, BlockPos pos) {
        var color = DyeColor.getColor(stack);
        var currentColor = blockState.getValue(property);

        if(color != null && color != currentColor && isAllowed(property, color)) {
            if(!level.isClientSide)
                set(property, blockState, color, level, pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static void modifyCloneItemStack(Property<DyeColor> property, ItemStack stack, BlockState blockState, boolean includeData, @Nullable Player player) {
        blockState.getOptionalValue(property).ifPresent(color -> {
            if(includeData || (player != null && player.isCreative()))
                stack.set(DataComponents.BASE_COLOR, color);
        });
    }

    public static void set(Property<DyeColor> property, BlockState blockState, DyeColor color, Level level, BlockPos pos) {
        var newBlockState = blockState.setValue(property, color);
        level.setBlock(pos, newBlockState, Block.UPDATE_ALL);

        BlockComponentHelper.runForComponent(blockState, BlockComponentTypes.MULTI_BLOCK, component -> {
            var origin = component.getOrigin(pos, newBlockState);
            var index = component.indexOf(newBlockState);

            for(var i = 0; i < component.size(); i++) {
                if(i == index)
                    continue;

                var newSubBlockState = component.withIndex(newBlockState, i);
                var subPos = component.getPos(origin, newSubBlockState);
                level.setBlock(subPos, newSubBlockState, Block.UPDATE_ALL);
            }
        });
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
