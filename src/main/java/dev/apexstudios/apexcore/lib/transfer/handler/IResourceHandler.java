package dev.apexstudios.apexcore.lib.transfer.handler;

import dev.apexstudios.apexcore.lib.transfer.resource.IResource;
import dev.apexstudios.apexcore.lib.transfer.transaction.TransactionContext;

public interface IResourceHandler<T extends IResource> {
    int size();

    T getResource(int index);

    int getAmount(int index);

    int getCapacity(int index, T resource);

    default boolean isValid(int index, T resource) {
        return true;
    }

    default boolean supportsInsertion(int index) {
        return true;
    }

    default boolean supportsInsertion() {
        var size = size();

        for(var i = 0; i < size; i++) {
            if(!supportsInsertion(i))
                return false;
        }

        return true;
    }

    default boolean supportsExtraction(int index) {
        return true;
    }

    default boolean supportsExtraction() {
        var size = size();

        for(var i = 0; i < size; i++) {
            if(!supportsExtraction(i))
                return false;
        }

        return true;
    }

    int insert(int index, T resource, int amount, TransactionContext transaction);

    int insert(T resource, int amount, TransactionContext transaction);

    int extract(int index, T resource, int amount, TransactionContext transaction);

    int extract(T resource, int amount, TransactionContext transaction);
}
