package dev.apexstudios.apexcore.lib.data.provider.model;

import com.mojang.math.Quadrant;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.Property;

public interface ModelUtil {
    static void horizontalFacingBlock(Block block, Property<Direction> property, BlockModelGenerators blockModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(block)))
                .with(createHorizontalFacingDispatch(property))
        );
    }

    static void horizontalFacingBlock(BlockComponentHolder block, BlockModelGenerators blockModels) {
        var facingProperty = block.getComponentOrThrow(BlockComponentTypes.FACING).getProperty();
        horizontalFacingBlock(block.unwrap(), facingProperty, blockModels);
    }

    static PropertyDispatch<VariantMutator> createHorizontalFacingDispatch(Property<Direction> property) {
        return PropertyDispatch.modify(property)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
                .select(Direction.NORTH, BlockModelGenerators.NOP);
    }

    static PropertyDispatch<VariantMutator> createHorizontalFacingDispatch(BlockComponentHolder block) {
        return createHorizontalFacingDispatch(block.getComponentOrThrow(BlockComponentTypes.FACING).getProperty());
    }

    static PropertyDispatch<MultiVariant> createMultiBlockDispatch(BlockComponentHolder block, IntFunction<ResourceLocation> modelGetter) {
        return PropertyDispatch.initial(block.getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).property())
                .generate(index -> BlockModelGenerators.plainVariant(modelGetter.apply(index)));
    }

    static void multiBlockModel(BlockComponentHolder block, BlockModelGenerators blockModels, IntFunction<ResourceLocation> modelGetter) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block.unwrap())
                .with(createMultiBlockDispatch(block, modelGetter))
                .with(createHorizontalFacingDispatch(block))
        );
    }

    static void multiBlockModelSuffix(BlockComponentHolder block, BlockModelGenerators blockModels, IntFunction<String> suffixGetter) {
        multiBlockModel(block, blockModels, index -> ModelLocationUtils.getModelLocation(block.unwrap(), suffixGetter.apply(index)));
    }

    static void registerBlockItemModel(Block block, BlockModelGenerators blockModels) {
        var item = block.asItem();
        blockModels.registerSimpleItemModel(item, ModelLocationUtils.getModelLocation(item));
    }

    static <TValue extends Comparable<TValue>> void facingPropertyModel(BlockComponentHolder block, BlockModelGenerators blockModels, Function<Block, Property<TValue>> propertyGetter, Function<TValue, ResourceLocation> modelGetter) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block.unwrap())
                .with(PropertyDispatch.initial(propertyGetter.apply(block.unwrap())).generate(value -> BlockModelGenerators.plainVariant(modelGetter.apply(value))))
                .with(createHorizontalFacingDispatch(block))
        );
    }

    static <TValue extends Comparable<TValue>> void facingPropertyModel(BlockComponentHolder block, BlockModelGenerators blockModels, Property<TValue> property, Function<TValue, ResourceLocation> modelGetter) {
        facingPropertyModel(block, blockModels, $ -> property, modelGetter);
    }

    static <TValue extends Comparable<TValue>> void facingPropertyModelSuffix(BlockComponentHolder block, BlockModelGenerators blockModels, Function<Block, Property<TValue>> propertyGetter, Function<TValue, String> suffixGetter) {
        facingPropertyModel(block, blockModels, propertyGetter, value -> ModelLocationUtils.getModelLocation(block.unwrap(), suffixGetter.apply(value)));
    }

    static <TValue extends Comparable<TValue>> void facingPropertyModelSuffix(BlockComponentHolder block, BlockModelGenerators blockModels, Property<TValue> property, Function<TValue, String> suffixGetter) {
        facingPropertyModelSuffix(block, blockModels, $ -> property, suffixGetter);
    }

    static void registerMultiBlockItemModel(BlockComponentHolder block, IntFunction<ResourceLocation> modelGetter, BlockModelGenerators blockModels) {
        blockModels.itemModelOutput.accept(block.unwrap().asItem(), ItemModelUtils.composite(IntStream
                .range(0, block.getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).size())
                .mapToObj(modelGetter)
                .map(ItemModelUtils::plainModel)
                .toArray(ItemModel.Unbaked[]::new)
        ));
    }

    static void registerMultiBlockItemModelSuffix(BlockComponentHolder block, IntFunction<String> suffixGetter, BlockModelGenerators blockModels) {
        registerMultiBlockItemModel(block, index -> ModelLocationUtils.getModelLocation(block.unwrap(), suffixGetter.apply(index)), blockModels);
    }

    static void registerCompositeBlockItemModel(Block block, BlockModelGenerators blockModels, ItemModel.Unbaked... models) {
        blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.composite(models));
    }

    static void registerCompositeBlockItemModel(Block block, BlockModelGenerators blockModels, ResourceLocation... models) {
        registerCompositeBlockItemModel(block, blockModels, Stream.of(models).map(ItemModelUtils::plainModel).toArray(ItemModel.Unbaked[]::new));
    }

    static void registerCompositeBlockItemModel(Block block, BlockModelGenerators blockModels, String... suffixes) {
        registerCompositeBlockItemModel(block, blockModels, Stream.of(suffixes).map(suffix -> ModelLocationUtils.getModelLocation(block, suffix)).toArray(ResourceLocation[]::new));
    }

    static Quadrant facingToModelRotation(Direction facing) {
        return switch (facing) {
            case EAST -> Quadrant.R90;
            case SOUTH -> Quadrant.R180;
            case WEST -> Quadrant.R270;
            default -> Quadrant.R0;
        };
    }

    static Rotation facingToRotation(Direction facing) {
        return switch (facing) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
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
