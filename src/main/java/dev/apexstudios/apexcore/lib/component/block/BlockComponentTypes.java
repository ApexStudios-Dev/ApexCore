package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.block.types.BaseCauldronBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.BedBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.BounceBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.CauldronBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.DyeableBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.FacingBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.FluidLoggedBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.LayeredCauldronBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.MultiBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.RotationBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.types.SeatBlockComponent;
import org.jetbrains.annotations.ApiStatus;

public interface BlockComponentTypes {
    BlockComponentType<CauldronBlockComponent, BaseCauldronBlockComponent.SimpleBuilder> CAULDRON = CauldronBlockComponent.COMPONENT_TYPE;
    BlockComponentType<LayeredCauldronBlockComponent, LayeredCauldronBlockComponent.Builder> LAYERED_CAULDRON = LayeredCauldronBlockComponent.COMPONENT_TYPE;
    BlockComponentType<MultiBlockComponent, MultiBlockComponent.Builder> MULTI_BLOCK = MultiBlockComponent.COMPONENT_TYPE;
    BlockComponentType<FacingBlockComponent, FacingBlockComponent.Builder> FACING = FacingBlockComponent.COMPONENT_TYPE;
    BlockComponentType<RotationBlockComponent, RotationBlockComponent.Builder> ROTATION = RotationBlockComponent.COMPONENT_TYPE;
    BlockComponentType<FluidLoggedBlockComponent, FluidLoggedBlockComponent.Builder> FLUID_LOGGED = FluidLoggedBlockComponent.COMPONENT_TYPE;
    BlockComponentType<SeatBlockComponent, Object> SEAT = SeatBlockComponent.COMPONENT_TYPE;
    BlockComponentType<BedBlockComponent, BedBlockComponent.Builder> BED = BedBlockComponent.COMPONENT_TYPE;
    BlockComponentType<BounceBlockComponent, Object> BOUNCE = BounceBlockComponent.COMPONENT_TYPE;
    BlockComponentType<DyeableBlockComponent, DyeableBlockComponent.Builder> DYEABLE = DyeableBlockComponent.COMPONENT_TYPE;

    @ApiStatus.Internal
    static void register() {

    }
}
