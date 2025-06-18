package dev.apexstudios.apexcore.lib.transfer.handler;

import dev.apexstudios.apexcore.lib.transfer.resource.IResource;
import dev.apexstudios.apexcore.lib.transfer.transaction.TransactionContext;

public abstract class CommonIOResourceHandler<T extends IResource> implements IResourceHandler<T> {
    protected abstract int insertCommon(int index, T resource, int amount, TransactionContext transaction);

    protected abstract int extractCommon(int index, T resource, int amount, TransactionContext transaction);

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        if(ResourceHandlerUtil.isEmpty(resource, amount))
            return 0;

        return insertCommon(index, resource, amount, transaction);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if(ResourceHandlerUtil.isEmpty(resource, amount))
            return 0;

        var size = size();
        var handled = 0;

        for(var i = 0; i < size; i++) {
            handled += insertCommon(i, resource, amount, transaction);

            if(handled >= amount)
                break;
        }

        return handled;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        if(ResourceHandlerUtil.isEmpty(resource, amount))
            return 0;

        return extractCommon(index, resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if(ResourceHandlerUtil.isEmpty(resource, amount))
            return 0;

        var size = size();
        var handled = 0;

        for(var i = 0; i < size; i++) {
            handled += extractCommon(i, resource, amount, transaction);

            if(handled >= amount)
                break;
        }

        return handled;
    }
}
