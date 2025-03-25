package dev.apexstudios.apexcore.lib.data.provider.model;

import com.mojang.math.Quadrant;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
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

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> void horizontalFacingBlock(TBlock block, BlockModelGenerators blockModels) {
        var facingProperty = block.getComponentOrThrow(BlockComponentTypes.FACING).getProperty();
        horizontalFacingBlock(block, facingProperty, blockModels);
    }

    static PropertyDispatch<VariantMutator> createHorizontalFacingDispatch(Property<Direction> property) {
        return PropertyDispatch.modify(property)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
                .select(Direction.NORTH, BlockModelGenerators.NOP);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> PropertyDispatch<VariantMutator> createHorizontalFacingDispatch(TBlock block) {
        return createHorizontalFacingDispatch(block.getComponentOrThrow(BlockComponentTypes.FACING).getProperty());
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> PropertyDispatch<MultiVariant> createMultiBlockDispatch(TBlock block, IntFunction<ResourceLocation> modelGetter) {
        return PropertyDispatch.initial(block.getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).property())
                .generate(index -> BlockModelGenerators.plainVariant(modelGetter.apply(index)));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> void multiBlockModel(TBlock block, BlockModelGenerators blockModels, IntFunction<ResourceLocation> modelGetter) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(createMultiBlockDispatch(block, modelGetter))
                .with(createHorizontalFacingDispatch(block))
        );
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> void multiBlockModelSuffix(TBlock block, BlockModelGenerators blockModels, IntFunction<String> suffixGetter) {
        multiBlockModel(block, blockModels, index -> ModelLocationUtils.getModelLocation(block, suffixGetter.apply(index)));
    }

    static void registerBlockItemModel(Block block, BlockModelGenerators blockModels) {
        var item = block.asItem();
        blockModels.registerSimpleItemModel(item, ModelLocationUtils.getModelLocation(item));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>, TValue extends Comparable<TValue>> void facingPropertyModel(TBlock block, BlockModelGenerators blockModels, Function<TBlock, Property<TValue>> propertyGetter, Function<TValue, ResourceLocation> modelGetter) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(propertyGetter.apply(block)).generate(value -> BlockModelGenerators.plainVariant(modelGetter.apply(value))))
                .with(createHorizontalFacingDispatch(block))
        );
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>, TValue extends Comparable<TValue>> void facingPropertyModel(TBlock block, BlockModelGenerators blockModels, Property<TValue> property, Function<TValue, ResourceLocation> modelGetter) {
        facingPropertyModel(block, blockModels, $ -> property, modelGetter);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>, TValue extends Comparable<TValue>> void facingPropertyModelSuffix(TBlock block, BlockModelGenerators blockModels, Function<TBlock, Property<TValue>> propertyGetter, Function<TValue, String> suffixGetter) {
        facingPropertyModel(block, blockModels, propertyGetter, value -> ModelLocationUtils.getModelLocation(block, suffixGetter.apply(value)));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>, TValue extends Comparable<TValue>> void facingPropertyModelSuffix(TBlock block, BlockModelGenerators blockModels, Property<TValue> property, Function<TValue, String> suffixGetter) {
        facingPropertyModelSuffix(block, blockModels, $ -> property, suffixGetter);
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> void registerMultiBlockItemModel(TBlock block, IntFunction<ResourceLocation> modelGetter, BlockModelGenerators blockModels) {
        blockModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.composite(IntStream
                .range(0, block.getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).size())
                .mapToObj(modelGetter)
                .map(ItemModelUtils::plainModel)
                .toArray(ItemModel.Unbaked[]::new)
        ));
    }

    static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> void registerMultiBlockItemModelSuffix(TBlock block, IntFunction<String> suffixGetter, BlockModelGenerators blockModels) {
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
