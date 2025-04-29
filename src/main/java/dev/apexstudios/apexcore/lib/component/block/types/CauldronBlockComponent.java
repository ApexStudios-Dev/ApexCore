package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class CauldronBlockComponent extends BaseCauldronBlockComponent<BaseCauldronBlockComponent.SimpleBuilder> {
    public static final BlockComponentType<CauldronBlockComponent, BaseCauldronBlockComponent.SimpleBuilder> COMPONENT_TYPE = BlockComponentType.register(
            ApexCore.identifier("cauldron"),
            BaseCauldronBlockComponent.SimpleBuilder::new,
            CauldronBlockComponent::new
    );

    private CauldronBlockComponent(BlockComponentHolder holder, SimpleBuilder builder) {
        super(holder, builder);
    }

    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        if(CauldronBlock.shouldHandlePrecipitation(level, precipitation)) {
            if(precipitation == Biome.Precipitation.RAIN) {
                level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState());
                level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            } else if(precipitation == Biome.Precipitation.SNOW) {
                level.setBlockAndUpdate(pos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
                level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            }
        }
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return true;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos pos, Fluid fluid) {
        if(fluid.getFluidType().handleCauldronDrip(fluid, level, pos))
            return;

        if(fluid == Fluids.WATER) {
            var newBlockState = Blocks.WATER_CAULDRON.defaultBlockState();
            level.setBlockAndUpdate(pos, newBlockState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newBlockState));
            level.levelEvent(LevelEvent.SOUND_DRIP_WATER_INTO_CAULDRON, pos, 0);
        } else if(fluid == Fluids.LAVA) {
            var newBlockState = Blocks.LAVA_CAULDRON.defaultBlockState();
            level.setBlockAndUpdate(pos, newBlockState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newBlockState));
            level.levelEvent(LevelEvent.SOUND_DRIP_LAVA_INTO_CAULDRON, pos, 0);
        }
    }
}
