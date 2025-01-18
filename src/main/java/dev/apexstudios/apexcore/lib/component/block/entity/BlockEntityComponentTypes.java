package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.types.InventoryBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LockBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LootTableBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.NameableBlockEntityComponent;
import org.jetbrains.annotations.ApiStatus;

public interface BlockEntityComponentTypes {
    ComponentType<BlockEntityComponent, NameableBlockEntityComponent, NameableBlockEntityComponent.Builder> NAMEABLE = NameableBlockEntityComponent.COMPONENT_TYPE;
    ComponentType<BlockEntityComponent, InventoryBlockEntityComponent, InventoryBlockEntityComponent.Builder> INVENTORY = InventoryBlockEntityComponent.COMPONENT_TYPE;
    ComponentType<BlockEntityComponent, LockBlockEntityComponent, ComponentBuilder> LOCK = LockBlockEntityComponent.COMPONENT_TYPE;
    ComponentType<BlockEntityComponent, LootTableBlockEntityComponent, ComponentBuilder> LOOT_TABLE = LootTableBlockEntityComponent.COMPONENT_TYPE;

    @ApiStatus.Internal
    static void register() {

    }
}
