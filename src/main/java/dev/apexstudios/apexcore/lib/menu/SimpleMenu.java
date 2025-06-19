package dev.apexstudios.apexcore.lib.menu;

import com.google.common.util.concurrent.Runnables;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.resources.IIndexModifier;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemStackListHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class SimpleMenu extends AbstractContainerMenu {
    private final int rowCount;

    public SimpleMenu(MenuType<? extends SimpleMenu> menuType, int containerId, Inventory inventory, IResourceHandler<ItemResource> itemHandler, IIndexModifier<ItemResource> setter) {
        super(menuType, containerId);

        rowCount = itemHandler.size() / SLOTS_PER_ROW;

        addItemHandlerSlots(itemHandler, setter);
        addStandardInventorySlots(inventory, 8, 18 + rowCount * SLOT_SIZE + 13);
    }

    public int rowCount() {
        return rowCount;
    }

    protected void addItemHandlerSlots(IResourceHandler<ItemResource> itemHandler, IIndexModifier<ItemResource> setter) {
        for(var i = 0; i < rowCount; i++) {
            for(var j = 0; j < SLOTS_PER_ROW; j++) {
                addSlot(new ResourceHandlerSlot(itemHandler, j + i * SLOTS_PER_ROW, 8 + j * SLOT_SIZE, 18 + i * SLOT_SIZE, setter));
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

    public static SimpleMenu forNetwork(MenuType<? extends SimpleMenu> menuType, int containerId, Inventory inventory, int rowCount) {
        var handler = new ItemStackListHandler(rowCount * SLOTS_PER_ROW, Item.ABSOLUTE_MAX_STACK_SIZE, Runnables.doNothing());
        return new SimpleMenu(menuType, containerId, inventory, handler, handler::set);
    }
}
