package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.types.LayeredCauldronBlockComponent;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
public interface BlockComponentTypes {
    ComponentType<BlockComponent, LayeredCauldronBlockComponent, Block, LayeredCauldronBlockComponent.Builder> LAYERED_CAULDRON = LayeredCauldronBlockComponent.COMPONENT_TYPE;

    @ApiStatus.Internal
    static void register() {

    }
}
