package dev.apexstudios.apexcore.lib.data.provider.model;

import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.Property;

public interface ModelUtil {
    static void horizontalFacingBlock(Block block, Property<Direction> property, BlockModelGenerators blockModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(createHorizontalFacingDispatch(property, (facing, variant) -> variant.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block))))
        );
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>> void horizontalFacingBlock(TBlock block, BlockModelGenerators blockModels) {
        var facingProperty = block.getComponentOrThrow(BlockComponentTypes.FACING).getProperty();
        horizontalFacingBlock(block, facingProperty, blockModels);
    }

    static PropertyDispatch createHorizontalFacingDispatch(Property<Direction> property, BiFunction<Direction, Variant, Variant> modifier) {
        return PropertyDispatch.property(property)
                .select(Direction.EAST, modifier.apply(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)))
                .select(Direction.SOUTH, modifier.apply(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)))
                .select(Direction.WEST, modifier.apply(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)))
                .select(Direction.NORTH, modifier.apply(Direction.NORTH, Variant.variant()));
    }

    static PropertyDispatch createHorizontalFacingDispatch(Property<Direction> property) {
        return createHorizontalFacingDispatch(property, (facing, variant) -> variant);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>> PropertyDispatch createHorizontalFacingDispatch(TBlock block, BiFunction<Direction, Variant, Variant> modifier) {
        return createHorizontalFacingDispatch(
                block.getComponentOrThrow(BlockComponentTypes.FACING).getProperty(),
                modifier
        );
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>> PropertyDispatch createHorizontalFacingDispatch(TBlock block) {
        return createHorizontalFacingDispatch(block, (facing, variant) -> variant);
    }

    static <TBlock extends Block, TValue extends Comparable<TValue>> PropertyDispatch createPropertyDispatch(TBlock block, Function<TBlock, Property<TValue>> propertyGetter, Function<TValue, ResourceLocation> modelGetter) {
        return PropertyDispatch.property(propertyGetter.apply(block))
                .generate(value -> Variant.variant().with(VariantProperties.MODEL, modelGetter.apply(value)));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>> void multiBlockModel(TBlock block, BlockModelGenerators blockModels, IntFunction<ResourceLocation> modelGetter) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(createPropertyDispatch(block, b -> block.getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).property(), modelGetter::apply))
                .with(createHorizontalFacingDispatch(block))
        );

        // registerMultiBlockItemModel(block, modelGetter, blockModels);
        registerBlockItemModel(block, blockModels);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>> void multiBlockModelSuffix(TBlock block, BlockModelGenerators blockModels, IntFunction<String> suffixGetter) {
        multiBlockModel(block, blockModels, index -> ModelLocationUtils.getModelLocation(block, suffixGetter.apply(index)));
    }

    static void registerBlockItemModel(Block block, BlockModelGenerators blockModels) {
        var item = block.asItem();
        blockModels.registerSimpleItemModel(item, ModelLocationUtils.getModelLocation(item));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>, TValue extends Comparable<TValue>> void facingPropertyModel(TBlock block, BlockModelGenerators blockModels, Function<TBlock, Property<TValue>> propertyGetter, Function<TValue, ResourceLocation> modelGetter, TValue itemValue) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(createPropertyDispatch(block, propertyGetter, modelGetter))
                .with(createHorizontalFacingDispatch(block))
        );

        blockModels.registerSimpleItemModel(block, modelGetter.apply(itemValue));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>, TValue extends Comparable<TValue>> void facingPropertyModel(TBlock block, BlockModelGenerators blockModels, Property<TValue> property, Function<TValue, ResourceLocation> modelGetter, TValue itemValue) {
        facingPropertyModel(block, blockModels, $ -> property, modelGetter, itemValue);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>, TValue extends Comparable<TValue>> void facingPropertyModelSuffix(TBlock block, BlockModelGenerators blockModels, Function<TBlock, Property<TValue>> propertyGetter, Function<TValue, String> suffixGetter, TValue itemValue) {
        facingPropertyModel(block, blockModels, propertyGetter, value -> ModelLocationUtils.getModelLocation(block, suffixGetter.apply(value)), itemValue);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>, TValue extends Comparable<TValue>> void facingPropertyModelSuffix(TBlock block, BlockModelGenerators blockModels, Property<TValue> property, Function<TValue, String> suffixGetter, TValue itemValue) {
        facingPropertyModelSuffix(block, blockModels, $ -> property, suffixGetter, itemValue);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>> void registerMultiBlockItemModel(TBlock block, IntFunction<ResourceLocation> modelGetter, BlockModelGenerators blockModels) {
        blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.composite(IntStream
                .range(0, block.getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).size())
                .mapToObj(modelGetter)
                .map(ItemModelUtils::plainModel)
                .toArray(ItemModel.Unbaked[]::new)
        ));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent>> void registerMultiBlockItemModelSuffix(TBlock block, IntFunction<String> suffixGetter, BlockModelGenerators blockModels) {
        registerMultiBlockItemModel(block, index -> ModelLocationUtils.getModelLocation(block, suffixGetter.apply(index)), blockModels);
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

    static VariantProperties.Rotation facingToModelRotation(Direction facing) {
        return switch (facing) {
            case EAST -> VariantProperties.Rotation.R90;
            case SOUTH -> VariantProperties.Rotation.R180;
            case WEST -> VariantProperties.Rotation.R270;
            default -> VariantProperties.Rotation.R0;
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

    static VariantProperties.Rotation rotation(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> VariantProperties.Rotation.R90;
            case CLOCKWISE_180 -> VariantProperties.Rotation.R180;
            case COUNTERCLOCKWISE_90 -> VariantProperties.Rotation.R270;
            default -> VariantProperties.Rotation.R0;
        };
    }

    static Rotation rotation(VariantProperties.Rotation rotation) {
        return switch (rotation) {
            case R90 -> Rotation.CLOCKWISE_90;
            case R180 -> Rotation.CLOCKWISE_180;
            case R270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
