package dev.apexstudios.apexcore.lib.util;

import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.GameData;

public interface ApexUtil {
    static void registerPoiBlockState(ResourceKey<PoiType> poiType, BlockState blockState) {
        var holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(poiType);
        GameData.getBlockStatePointOfInterestTypeMap().put(blockState, holder);
    }

    static void registerPoiBlockStates(ResourceKey<PoiType> poiType, Block block, Predicate<BlockState> filter) {
        for(var blockState : block.getStateDefinition().getPossibleStates()) {
            if(filter.test(blockState))
                registerPoiBlockState(poiType, blockState);
        }
    }
}
