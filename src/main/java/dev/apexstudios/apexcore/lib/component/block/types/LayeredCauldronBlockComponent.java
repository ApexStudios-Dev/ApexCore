package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public final class LayeredCauldronBlockComponent extends BaseCauldronBlockComponent<LayeredCauldronBlockComponent.Builder> {
    public static final ComponentType<BlockComponent, LayeredCauldronBlockComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("layered_cauldron"),
            Builder::new,
            LayeredCauldronBlockComponent::new
    );

    public static final IntegerProperty LEVEL = LayeredCauldronBlock.LEVEL;
    public static final int MIN_FILL_LEVEL = LayeredCauldronBlock.MIN_FILL_LEVEL;
    public static final int MAX_FILL_LEVEL = LayeredCauldronBlock.MAX_FILL_LEVEL;

    private final Biome.Precipitation precipitation;

    private LayeredCauldronBlockComponent(ComponentHolder<BlockComponent> holder, Builder builder) {
        super(holder, builder);

        precipitation = builder.precipitation;
    }

    private boolean isFull(BlockState blockState) {
        return blockState.getValue(LEVEL) == MAX_FILL_LEVEL;
    }

    private double getContentHeight(BlockState blockState) {
        return (6D + blockState.getValue(LEVEL) * 3D) / 16D;
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(LEVEL);
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState.setValue(LEVEL, MIN_FILL_LEVEL);
    }

    @Override
    protected void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos pos, Fluid fluid) {
        if(!isFull(blockState)) {
            var newBlockState = blockState.setValue(LEVEL, blockState.getValue(LEVEL) + 1);
            level.setBlockAndUpdate(pos, newBlockState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newBlockState));
            level.levelEvent(LevelEvent.SOUND_DRIP_WATER_INTO_CAULDRON, pos, 0);
        }
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity) {
        var contentHeight = getContentHeight(blockState);

        if(level instanceof ServerLevel sLevel && entity.isOnFire() && isEntityInsideContent(pos, entity, contentHeight)) {
            entity.clearFire();

            if(entity.mayInteract(sLevel, pos))
                handleEntityOnFireInside(blockState, level, pos);
        }
    }

    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        if(CauldronBlock.shouldHandlePrecipitation(level, precipitation) && !isFull(blockState) && precipitation == this.precipitation) {
            var newBlockState = blockState.cycle(LEVEL);
            level.setBlockAndUpdate(pos, newBlockState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newBlockState));
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return blockState.getValue(LEVEL);
    }

    private void handleEntityOnFireInside(BlockState blockState, Level level, BlockPos pos) {
        if(precipitation == Biome.Precipitation.SNOW)
            LayeredCauldronBlock.lowerFillLevel(Blocks.WATER_CAULDRON.withPropertiesOf(blockState), level, pos);
        else
            LayeredCauldronBlock.lowerFillLevel(blockState, level, pos);
    }

    public static final class Builder extends BaseCauldronBlockComponent.Builder<Builder> {
        private Biome.Precipitation precipitation = Biome.Precipitation.NONE;

        public Builder precipitation(Biome.Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }
    }
}
