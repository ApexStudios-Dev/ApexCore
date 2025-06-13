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
    private final int rowCount;

    public SimpleMenu(MenuType<? extends SimpleMenu> menuType, int containerId, Inventory inventory, IItemHandler itemHandler) {
        super(menuType, containerId);

        rowCount = itemHandler.getSlots() / SLOTS_PER_ROW;

        addItemHandlerSlots(itemHandler);
        addStandardInventorySlots(inventory, 8, 18 + rowCount * SLOT_SIZE + 13);
    }

    public SimpleMenu(MenuType<? extends SimpleMenu> menuType, int containerId, Inventory inventory, int rowCount) {
        this(menuType, containerId, inventory, new ItemStackHandler(rowCount * SLOTS_PER_ROW));
    }

    public int rowCount() {
        return rowCount;
    }

    protected void addItemHandlerSlots(IItemHandler itemHandler) {
        for(var i = 0; i < rowCount; i++) {
            for(var j = 0; j < SLOTS_PER_ROW; j++) {
                addSlot(new SlotItemHandler(itemHandler, j + i * SLOTS_PER_ROW, 8 + j * SLOT_SIZE, 18 + i * SLOT_SIZE));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = slots.get(index);

        if(!slot.hasItem())
            return ItemStack.EMPTY;

        var stack = slot.getItem();
        var stack1 = stack.copy();

        if(index < rowCount * SLOTS_PER_ROW) {
            if(!moveItemStackTo(stack1, rowCount * SLOTS_PER_ROW, slots.size(), true))
                return ItemStack.EMPTY;
        } else if(!moveItemStackTo(stack1, 0, rowCount * SLOTS_PER_ROW, false))
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
