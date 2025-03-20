package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.types.InventoryBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LockBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.LootTableBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.types.NameableBlockEntityComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

public interface BlockEntityComponentTypes {
    ComponentType<BlockEntityComponent, NameableBlockEntityComponent, BlockEntity, NameableBlockEntityComponent.Builder> NAMEABLE = NameableBlockEntityComponent.COMPONENT_TYPE;
    ComponentType<BlockEntityComponent, InventoryBlockEntityComponent, BlockEntity, InventoryBlockEntityComponent.Builder> INVENTORY = InventoryBlockEntityComponent.COMPONENT_TYPE;
    ComponentType<BlockEntityComponent, LockBlockEntityComponent, BlockEntity, ComponentBuilder> LOCK = LockBlockEntityComponent.COMPONENT_TYPE;
    ComponentType<BlockEntityComponent, LootTableBlockEntityComponent, BlockEntity, ComponentBuilder> LOOT_TABLE = LootTableBlockEntityComponent.COMPONENT_TYPE;

    @ApiStatus.Internal
    static void register() {

    }
}
