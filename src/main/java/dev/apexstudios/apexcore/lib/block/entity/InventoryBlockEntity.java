package dev.apexstudios.apexcore.lib.block.entity;

import dev.apexstudios.apexcore.lib.menu.SimpleMenu;
import dev.apexstudios.apexcore.lib.multiblock.MultiBlock;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class InventoryBlockEntity extends BlockEntity implements MenuProvider {
    public static final String TAG_INVENTORY = "Inventory";
    public static final String TAG_NAME = "CustomName";

    protected final ItemStackHandler inventory;
    @Nullable protected Component customName;

    protected InventoryBlockEntity(BlockEntityType<? extends InventoryBlockEntity> blockEntityType, BlockPos pos, BlockState blockState, int rows, int cols) {
        this(blockEntityType, pos, blockState, rows * cols);
    }

    protected InventoryBlockEntity(BlockEntityType<? extends InventoryBlockEntity> blockEntityType, BlockPos pos, BlockState blockState, int slots) {
        super(blockEntityType, pos, blockState);

        inventory = new ItemStackHandler(slots) {
            @Override
            public int getSlotLimit(int slot) {
                return InventoryBlockEntity.this.getSlotLimit(slot);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return canExtract(slot) ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return canInsert(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                onSlotChanged(slot);
                setChanged();
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
    public IItemHandlerModifiable getItemHandler() {
        return inventory;
    }

    public NonNullList<ItemStack> getItems() {
        var items = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);

        for(var i = 0; i < inventory.getSlots(); i++) {
            items.set(i, inventory.getStackInSlot(i));
        }

        return items;
    }

    protected int getSlotLimit(int slot) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    protected boolean canExtract(int slot) {
        return true;
    }

    protected boolean canInsert(int slot, ItemStack stack) {
        return true;
    }

    protected void onSlotChanged(int slot) {

    }
    // endregion

    // region: Overrides
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        customName = parseCustomNameSafe(tag.getCompound(TAG_NAME).orElse(null), registries);
        inventory.deserializeNBT(registries, tag.getCompoundOrEmpty(TAG_INVENTORY));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.storeNullable(TAG_NAME, ComponentSerialization.CODEC, registries.createSerializationContext(NbtOps.INSTANCE), customName);
        tag.put(TAG_INVENTORY, inventory.serializeNBT(registries));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        customName = components.get(DataComponents.CUSTOM_NAME);

        var contents = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        for(var i = 0; i < contents.getSlots(); i++) {
            inventory.setStackInSlot(i, contents.getStackInSlot(i));
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, customName);
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(TAG_NAME);
        tag.remove(TAG_INVENTORY);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        super.preRemoveSideEffects(pos, blockState);

        if(level != null) {
            for(var i = 0; i < inventory.getSlots(); i++) {
                var stack = inventory.getStackInSlot(i);
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        var menuType = menuType();
        return menuType == null ? null : new SimpleMenu((MenuType<? extends SimpleMenu>) menuType, windowId, playerInventory, inventory);
    }
    // endregion

    @Nullable
    public static IItemHandler inventoryProvider(Level level, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable Direction facing) {
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
        event.registerBlock(Capabilities.ItemHandler.BLOCK, InventoryBlockEntity::inventoryProvider, blockEntityType.get().getValidBlocks().toArray(Block[]::new));
    }
}
