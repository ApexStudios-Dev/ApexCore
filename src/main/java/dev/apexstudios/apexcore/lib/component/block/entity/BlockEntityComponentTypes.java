package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.block.entity.types.InventoryBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LockBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LootTableBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.NameableBlockEntityComponent;
import org.jetbrains.annotations.ApiStatus;

public interface BlockEntityComponentTypes {
    BlockEntityComponentType<NameableBlockEntityComponent, NameableBlockEntityComponent.Builder> NAMEABLE = NameableBlockEntityComponent.COMPONENT_TYPE;
    BlockEntityComponentType<InventoryBlockEntityComponent, InventoryBlockEntityComponent.Builder> INVENTORY = InventoryBlockEntityComponent.COMPONENT_TYPE;
    BlockEntityComponentType<LockBlockEntityComponent, Object> LOCK = LockBlockEntityComponent.COMPONENT_TYPE;
    BlockEntityComponentType<LootTableBlockEntityComponent, Object> LOOT_TABLE = LootTableBlockEntityComponent.COMPONENT_TYPE;

    @ApiStatus.Internal
    static void register() {

    }
}
