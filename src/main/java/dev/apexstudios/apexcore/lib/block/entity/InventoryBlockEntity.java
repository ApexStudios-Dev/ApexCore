package dev.apexstudios.apexcore.lib.block.entity;

import dev.apexstudios.apexcore.lib.menu.SimpleMenu;
import dev.apexstudios.apexcore.lib.multiblock.MultiBlock;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemStackListHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.Nullable;

public class InventoryBlockEntity extends BlockEntity implements MenuProvider {
    public static final String TAG_NAME = "CustomName";
    public static final String TAG_ITEMS = "Items";
    public static final String TAG_SIZE = "Size";

    protected final ItemStackListHandler inventory;
    @Nullable protected Component customName;

    protected InventoryBlockEntity(BlockEntityType<? extends InventoryBlockEntity> blockEntityType, BlockPos pos, BlockState blockState, int rows, int cols) {
        this(blockEntityType, pos, blockState, rows * cols);
    }

    protected InventoryBlockEntity(BlockEntityType<? extends InventoryBlockEntity> blockEntityType, BlockPos pos, BlockState blockState, int slots) {
        super(blockEntityType, pos, blockState);

        inventory = new ItemStackListHandler(slots, Item.ABSOLUTE_MAX_STACK_SIZE, this::setChanged) {
            @Override
            public boolean isValid(int index, ItemResource resource) {
                return InventoryBlockEntity.this.canInsert(index, resource);
            }
        };
    }

    @Nullable
    protected MenuType<?> menuType() {
        return null;
    }

    // region: Nameable
    public void setCustomName(@Nullable Component customName) {
        if(this.customName != customName) {
            this.customName = customName;
            setChanged();
        }
    }

    public Component getDisplayName() {
        return Objects.requireNonNullElseGet(customName, this::getDefaultName);
    }

    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }
    // endregion

    // region: ItemHandler
    public IResourceHandler<ItemResource> getItemHandler() {
        return inventory;
    }

    public NonNullList<ItemStack> getItems() {
        return inventory.copyToList();
    }

    protected boolean canInsert(int slot, ItemResource resource) {
        return true;
    }
    // endregion

    // region: Overrides
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        customName = parseCustomNameSafe(input, TAG_NAME);
        // TODO: inventory.deserialize(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(TAG_NAME, ComponentSerialization.CODEC, customName);
        // TODO: inventory.serialize(output);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        customName = components.get(DataComponents.CUSTOM_NAME);

        var contents = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        for(var i = 0; i < contents.getSlots(); i++) {
            var stack = contents.getStackInSlot(i);
            inventory.set(i, ItemResource.of(stack), stack.getCount());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, customName);
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(TAG_NAME);
        output.discard(TAG_ITEMS);
        output.discard(TAG_SIZE);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        super.preRemoveSideEffects(pos, blockState);

        if(level != null) {
            for(var i = 0; i < inventory.size(); i++) {
                var stack = inventory.getResource(i).toStack(inventory.getAmount(i));
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        var menuType = menuType();
        return menuType == null ? null : new SimpleMenu((MenuType<? extends SimpleMenu>) menuType, windowId, playerInventory, inventory, inventory::set);
    }
    // endregion

    @Nullable
    public static IResourceHandler<ItemResource> inventoryProvider(Level level, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable Direction facing) {
        // while this lookup uses `MultiBlock` it works just fine for none multi block types
        // if the given block is a multi block, the block entity is looked up using the origin point
        // if the given block is not a multi block, the block entity is looked up using the given 'pos'
        if(blockEntity == null)
            blockEntity = MultiBlock.getBlockEntity(level, pos, blockState);
        if(blockEntity instanceof InventoryBlockEntity inventory)
            return inventory.getItemHandler();

        return null;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, Supplier<? extends BlockEntityType<? extends InventoryBlockEntity>> blockEntityType) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, blockEntityType.get(), (blockEntity, facing) -> blockEntity.getItemHandler());

        var validBlocks = blockEntityType.get().getValidBlocks();

        if(!validBlocks.isEmpty())
            event.registerBlock(Capabilities.ItemHandler.BLOCK, InventoryBlockEntity::inventoryProvider, validBlocks.toArray(Block[]::new));
    }
}
