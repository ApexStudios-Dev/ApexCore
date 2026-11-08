package dev.apexstudios.apexcore.extensions;

import dev.apexstudios.apexcore.api.FakeItemEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.fluids.FluidType;

public interface FluidTypeExtensions {
    /// @see FluidType#setItemMovement(ItemEntity)
    default void setItemMovement(FakeItemEntity entity) {
        entity.setFluidMovement(.99F); // match neos extension
    }
}
