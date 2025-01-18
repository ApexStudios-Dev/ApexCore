package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.types.BaseCauldronBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.CauldronBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.FacingBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.FluidLoggedBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.LayeredCauldronBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.RotationBlockComponent;
import dev.apexstudios.apexcore.lib.multiblock.MultiBlockComponent;
import dev.apexstudios.apexcore.lib.seat.SeatBlockComponent;
import org.jetbrains.annotations.ApiStatus;

public interface BlockComponentTypes {
    ComponentType<BlockComponent, CauldronBlockComponent, BaseCauldronBlockComponent.SimpleBuilder> CAULDRON = CauldronBlockComponent.COMPONENT_TYPE;
    ComponentType<BlockComponent, LayeredCauldronBlockComponent, LayeredCauldronBlockComponent.Builder> LAYERED_CAULDRON = LayeredCauldronBlockComponent.COMPONENT_TYPE;
    ComponentType<BlockComponent, MultiBlockComponent, MultiBlockComponent.Builder> MULTI_BLOCK = MultiBlockComponent.COMPONENT_TYPE;
    ComponentType<BlockComponent, FacingBlockComponent, FacingBlockComponent.Builder> FACING = FacingBlockComponent.COMPONENT_TYPE;
    ComponentType<BlockComponent, RotationBlockComponent, RotationBlockComponent.Builder> ROTATION = RotationBlockComponent.COMPONENT_TYPE;
    ComponentType<BlockComponent, FluidLoggedBlockComponent, FluidLoggedBlockComponent.Builder> FLUID_LOGGED = FluidLoggedBlockComponent.COMPONENT_TYPE;
    ComponentType<BlockComponent, SeatBlockComponent, ComponentBuilder> SEAT = SeatBlockComponent.BLOCK_COMPONENT_TYPE;

    @ApiStatus.Internal
    static void register() {

    }
}
