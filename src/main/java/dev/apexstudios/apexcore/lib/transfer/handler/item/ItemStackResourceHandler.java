package dev.apexstudios.apexcore.lib.transfer.handler.item;

import com.google.common.collect.Lists;
import dev.apexstudios.apexcore.lib.transfer.handler.CommonIOResourceHandler;
import dev.apexstudios.apexcore.lib.transfer.resource.item.ItemResource;
import dev.apexstudios.apexcore.lib.transfer.transaction.SimpleSnapshotJournal;
import dev.apexstudios.apexcore.lib.transfer.transaction.TransactionContext;
import java.util.ArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemStackResourceHandler extends CommonIOResourceHandler<ItemResource> implements IItemResourceHandler {
    private final NonNullList<ItemStack> stacks;
    private final ArrayList<SimpleSnapshotJournal<ItemStack>> journals = Lists.newArrayList();

    public ItemStackResourceHandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        journals.ensureCapacity(size);

        for(var i = 0; i < size; i++) {
            var index = i;
            journals.add(new SimpleSnapshotJournal<>(
                    () -> stacks.get(index).copy(),
                    stack -> stacks.set(index, stack),
                    () -> onContentsChange(index)
            ));
        }
    }

    public void set(int index, ItemResource resource, int amount) {
        stacks.set(index, resource.toStack(amount));
    }

    protected void onContentsChange(int index) {

    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(stacks.get(index));
    }

    @Override
    public int getAmount(int index) {
        return stacks.get(index).getCount();
    }

    @Override
    protected int insertCommon(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if(!isValid(index, resource))
            return 0;

        var currentStack = stacks.get(index);
        var capacity = getCapacity(index, resource);
        int inserted;
        int newAmount;

        if(currentStack.isEmpty()) {
            inserted = Math.min(capacity, amount);
            newAmount = inserted;
        } else {
            if(!resource.is(currentStack))
                return 0;

            var currentStackAmount = currentStack.getCount();
            inserted = Math.min(capacity - currentStackAmount, amount);
            newAmount = currentStackAmount + inserted;
        }

        if(inserted > 0) {
            journals.get(index).updateSnapshots(transaction);
            set(index, resource, newAmount);
        }

        return inserted;
    }

    @Override
    protected int extractCommon(int index, ItemResource resource, int amount, TransactionContext transaction) {
        var currentStack = stacks.get(index);

        if(!resource.is(currentStack))
            return 0;

        var currentAmount = currentStack.getCount();
        var handledAmount = Math.min(amount, currentAmount);

        if(handledAmount > 0) {
            journals.get(index).updateSnapshots(transaction);
            set(index, resource, currentAmount - handledAmount);
        }

        return handledAmount;
    }

    @Override
    public IItemHandler toLegacy() {
        return new LegacyItemHandler.Modifiable(this);
    }
}
