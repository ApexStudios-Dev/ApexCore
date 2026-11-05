package dev.apexstudios.apexcore.api;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public interface BlockHelper {
    static BlockState copyProperties(BlockState source, BlockState target, Property<?>... properties) {
        var result = target;

        for(var property : properties) {
            result = BlockState.copyProperty(source, result, property);
        }

        return result;
    }
}
