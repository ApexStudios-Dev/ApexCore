package dev.apexstudios.apexcore.lib.transfer.resource;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public interface IDataComponentHolderResource extends IResource, DataComponentHolder {
    DataComponentPatch getComponentsPatch();

    boolean hasNonDefault(DataComponentType<?> componentType);

    default boolean hasNonDefault(Supplier<? extends DataComponentType<?>> componentType) {
        return hasNonDefault(componentType.get());
    }

    boolean isComponentsPatchEmpty();

    interface Prototyped extends IDataComponentHolderResource {
        DataComponentMap getPrototype();
    }
}
