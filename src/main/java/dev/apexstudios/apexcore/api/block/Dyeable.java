package dev.apexstudios.apexcore.api.block;

import com.mojang.serialization.Codec;
import dev.apexstudios.registree.Registree;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jspecify.annotations.Nullable;

public interface Dyeable {
    DataComponentType<DyeColor> COMPONENT = DataComponents.BASE_COLOR;

    boolean isBlankDyedColor(DyedColor color);

    DyedColor getCorrectedColor(DyedColor color);

    BlockState setDyedColor(BlockState blockState, DyedColor color);

    DyedColor getDyedColor(BlockState blockState);

    default void setDyedColor(MutableDataComponentHolder components, DyedColor color) {
        if(isBlankDyedColor(color)) {
            components.remove(COMPONENT);
        } else if(color != getDyedColor(components)) {
            components.set(COMPONENT, color.color);
        }
    }

    default DyedColor getDyedColor(DataComponentGetter components) {
        return getCorrectedColor(DyedColor.from(components.get(COMPONENT)));
    }

    default void setDyedColor(Level level, BlockPos pos, BlockState blockState, DyedColor color) {
        if(!level.isClientSide()) {
            level.setBlockAndUpdate(pos, setDyedColor(blockState, getCorrectedColor(color)));
        }
    }

    default DyedColor getDyedColorForPlacement(BlockPlaceContext context) {
        var player = context.getPlayer();

        if(player == null) {
            return getCorrectedColor(DyedColor.NONE);
        }

        var hand = context.getHand();
        var otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var stack = player.getItemInHand(otherHand);
        var color = getCorrectedColor(DyedColor.fromDye(stack));

        if(isBlankDyedColor(color)) {
            color = getDyedColor(context.getItemInHand());
        }

        return color;
    }

    default InteractionResult tryDyeBlock(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player) {
        var dye = getCorrectedColor(DyedColor.fromDye(stack));
        var currentColor = getDyedColor(blockState);

        if(player.isSecondaryUseActive()) {
            if(!isBlankDyedColor(currentColor)) {
                setDyedColor(level, pos, blockState, DyedColor.NONE);
                return InteractionResult.SUCCESS;
            }
        } else {
            if(!isBlankDyedColor(dye) && dye != currentColor) {
                setDyedColor(level, pos, blockState, dye);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    default void appendDyedColor(MutableDataComponentHolder components, BlockState blockState, @Nullable Player player, boolean includeData) {
        if(includeData || (player != null && player.isCreative())) {
            setDyedColor(components, getDyedColor(blockState));
        }
    }

    static Stream<Block> dyeableBlocks(Registree registree) {
        return registree.blocks().stream().filter(Dyeable.class::isInstance);
    }

    static Stream<Item> dyeableItems(Registree registree) {
        return dyeableBlocks(registree).map(ItemLike::asItem);
    }

    static void registerBlockColor(Registree registree, RegisterColorHandlersEvent.Block event) {
        event.register((blockState, level, pos, tintIndex) -> {
            if(tintIndex == 0 && blockState.getBlock() instanceof Dyeable dyeable) {
                var color = dyeable.getDyedColor(blockState).color;
                return color == null ? CommonColors.WHITE : color.getTextureDiffuseColor();
            }

            return CommonColors.WHITE;
        }, dyeableBlocks(registree).toArray(Block[]::new));
    }

    interface Colored extends Dyeable {
        EnumProperty<DyedColor> DYED_COLOR = EnumProperty.create("color", DyedColor.class, color -> color.color != null);

        default boolean isBlankDyedColor(DyedColor color) {
            return color == DyedColor.NONE || color == DyedColor.WHITE;
        }

        default DyedColor getCorrectedColor(DyedColor color) {
            return color == DyedColor.NONE ? DyedColor.WHITE : color;
        }

        default BlockState setDyedColor(BlockState blockState, DyedColor color) {
            return blockState.setValue(DYED_COLOR, getCorrectedColor(color));
        }

        default DyedColor getDyedColor(BlockState blockState) {
            return blockState.getValue(DYED_COLOR);
        }
    }

    interface WithNone extends Dyeable {
        EnumProperty<DyedColor> DYED_COLOR = EnumProperty.create("color", DyedColor.class);

        @Override
        default boolean isBlankDyedColor(DyedColor color) {
            return color == DyedColor.NONE;
        }

        @Override
        default DyedColor getCorrectedColor(DyedColor color) {
            return color;
        }

        @Override
        default BlockState setDyedColor(BlockState blockState, DyedColor color) {
            return blockState.setValue(DYED_COLOR, color);
        }

        @Override
        default DyedColor getDyedColor(BlockState blockState) {
            return blockState.getValue(DYED_COLOR);
        }
    }

    enum DyedColor implements StringRepresentable {
        NONE(null),
        WHITE(DyeColor.WHITE),
        ORANGE(DyeColor.ORANGE),
        MAGENTA(DyeColor.MAGENTA),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        PINK(DyeColor.PINK),
        GRAY(DyeColor.GRAY),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        CYAN(DyeColor.CYAN),
        PURPLE(DyeColor.PURPLE),
        BLUE(DyeColor.BLUE),
        BROWN(DyeColor.BROWN),
        GREEN(DyeColor.GREEN),
        RED(DyeColor.RED),
        BLACK(DyeColor.BLACK);

        public static final Codec<DyedColor> CODEC = StringRepresentable.fromEnum(DyedColor::values);

        @Nullable private final DyeColor color;

        DyedColor(@Nullable DyeColor color) {
            this.color = color;
        }

        @Nullable
        public DyeColor getColor() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return color == null ? "none" : color.getSerializedName();
        }

        public static DyedColor fromDye(ItemStack stack) {
            return from(DyeColor.getColor(stack));
        }

        public static DyedColor from(@Nullable DyeColor color) {
            return switch(color) {
                case WHITE -> WHITE;
                case ORANGE -> ORANGE;
                case MAGENTA -> MAGENTA;
                case LIGHT_BLUE -> LIGHT_BLUE;
                case YELLOW -> YELLOW;
                case LIME -> LIME;
                case PINK -> PINK;
                case GRAY -> GRAY;
                case LIGHT_GRAY -> LIGHT_GRAY;
                case CYAN -> CYAN;
                case PURPLE -> PURPLE;
                case BLUE -> BLUE;
                case BROWN -> BROWN;
                case GREEN -> GREEN;
                case RED -> RED;
                case BLACK -> BLACK;
                case null -> NONE;
            };
        }
    }
}
