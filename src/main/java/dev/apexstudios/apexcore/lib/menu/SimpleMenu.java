package dev.apexstudios.apexcore.lib.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SimpleMenu extends AbstractContainerMenu {
    private final int slotCount;

    public SimpleMenu(MenuType<? extends SimpleMenu> menuType, int containerId, Inventory inventory, IItemHandler itemHandler) {
        super(menuType, containerId);

        slotCount = itemHandler.getSlots();

        addItemHandlerSlots(itemHandler);
        addInventorySlots(inventory);
    }

    public SimpleMenu(MenuType<? extends SimpleMenu> menuType, int containerId, Inventory inventory, int slotCount) {
        this(menuType, containerId, inventory, new ItemStackHandler(slotCount));
    }

    public int slotCount() {
        return slotCount;
    }

    // assumes chest-like slot placement
    // overriders may also need to update (image)width/height in the matching screen
    protected void addItemHandlerSlots(IItemHandler itemHandler) {
        var cols = SLOTS_PER_ROW;
        var rows = slotCount / cols;

        var x = 8;
        var y = SLOT_SIZE - 4;

        for(var i = 0; i < rows; i++) {
            for(var j = 0; j < cols; j++) {
                addSlot(new SlotItemHandler(itemHandler, j + i * SLOTS_PER_ROW, x + j * SLOT_SIZE, y + i * SLOT_SIZE));
            }
        }
    }

    protected void addInventorySlots(Inventory inventory) {
        var cols = SLOTS_PER_ROW;
        var rows = slotCount / cols;
        var itemHandlerHeight = rows * SLOT_SIZE;

       addStandardInventorySlots(inventory, 8, itemHandlerHeight + SLOT_SIZE + 8);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = slots.get(index);

        if(!slot.hasItem())
            return ItemStack.EMPTY;

        var stack = slot.getItem();
        var stack1 = stack.copy();

        if(index < slotCount) {
            if(!moveItemStackTo(stack1, slotCount, slots.size(), true))
                return ItemStack.EMPTY;
        } else if(!moveItemStackTo(stack1, 0, slotCount, false))
            return ItemStack.EMPTY;

        if(stack1.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();

        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
