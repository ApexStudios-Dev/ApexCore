package dev.apexstudios.apexcore.lib.data.provider.model;

import com.mojang.math.Quadrant;
import dev.apexstudios.apexcore.lib.block.FacingBlock;
import java.util.function.Function;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.Property;

public interface ModelUtil {
    static <TBlock extends Block & FacingBlock> MultiVariantGenerator facingBlock(TBlock block, Function<Property<Direction>, PropertyDispatch<VariantMutator>> facingDispatchFactory) {
        return MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(block)))
                .with(facingDispatchFactory.apply(block.facingProperty()));
    }

    static <TBlock extends Block & FacingBlock> MultiVariantGenerator facingBlock(TBlock block) {
        return facingBlock(block, ModelUtil::createHorizontalFacingDispatch);
    }

    static PropertyDispatch<VariantMutator> createHorizontalFacingDispatch(Property<Direction> property) {
        return PropertyDispatch.modify(property)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
                .select(Direction.NORTH, BlockModelGenerators.NOP);
    }

    static <TBlock extends Block & FacingBlock> PropertyDispatch<VariantMutator> createFacingDispatch(TBlock block, Function<Direction, VariantMutator> facingMutator) {
        return PropertyDispatch.modify(block.facingProperty()).generate(facingMutator);
    }

    static <TBlock extends Block & FacingBlock> PropertyDispatch<VariantMutator> createHorizontalFacingDispatch(TBlock block) {
        return createFacingDispatch(block, facing -> switch (facing) {
            case EAST -> BlockModelGenerators.Y_ROT_90;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        });
    }

    static void registerBlockItemModel(Block block, BlockModelGenerators blockModels) {
        var item = block.asItem();
        blockModels.registerSimpleItemModel(item, ModelLocationUtils.getModelLocation(item));
    }

    static Quadrant rotation(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Quadrant.R90;
            case CLOCKWISE_180 -> Quadrant.R180;
            case COUNTERCLOCKWISE_90 -> Quadrant.R270;
            default -> Quadrant.R0;
        };
    }

    static Rotation rotation(Quadrant rotation) {
        return switch (rotation) {
            case R90 -> Rotation.CLOCKWISE_90;
            case R180 -> Rotation.CLOCKWISE_180;
            case R270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
