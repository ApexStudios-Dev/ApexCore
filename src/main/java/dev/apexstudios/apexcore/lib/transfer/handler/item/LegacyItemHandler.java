package dev.apexstudios.apexcore.lib.transfer.handler.item;

import dev.apexstudios.apexcore.lib.transfer.resource.item.ItemResource;
import dev.apexstudios.apexcore.lib.transfer.transaction.TransactionContext;
import dev.apexstudios.apexcore.lib.transfer.transaction.TransactionManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public sealed class LegacyItemHandler implements IItemHandler permits LegacyItemHandler.Modifiable {
    private final IItemResourceHandler handler;

    public LegacyItemHandler(IItemResourceHandler handler) {
        this.handler = handler;
    }

    protected IItemResourceHandler handler() {
        return handler;
    }

    @Override
    public int getSlots() {
        return handler().size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler().getResource(slot).toStack(handler().getAmount(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        try(var transaction = TransactionManager.open(TransactionContext.ROOT)) {
            var amount = stack.getCount();
            var inserted = handler().insert(slot, ItemResource.of(stack), amount, transaction);

            if(!simulate && inserted == amount)
                transaction.commit();

            var remainder = amount - inserted;
            return remainder <= 0 ? ItemStack.EMPTY : stack.copyWithCount(remainder);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        try(var transaction = TransactionManager.open(TransactionContext.ROOT)) {
            var resource = handler().getResource(slot);
            var extracted = handler().extract(slot, resource, amount, transaction);

            if(!simulate && extracted == amount)
                transaction.commit();

            return resource.toStack(extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler().getCapacity(slot, handler().getResource(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return handler().isValid(slot, ItemResource.of(stack));
    }

    public static final class Modifiable extends LegacyItemHandler implements IItemHandlerModifiable {
        private final Setter setter;

        public Modifiable(IItemResourceHandler handler, Setter setter) {
            super(handler);

            this.setter = setter;
        }

        public Modifiable(ItemStackResourceHandler handler) {
            this(handler, handler::set);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            setter.set(slot, ItemResource.of(stack), stack.getCount());
        }

        @FunctionalInterface
        public interface Setter {
            void set(int index, ItemResource resource, int amount);
        }
    }
}
