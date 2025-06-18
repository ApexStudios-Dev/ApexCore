package dev.apexstudios.apexcore.lib.transfer.transaction;

import com.google.common.collect.Lists;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public final class TransactionManager {
    private static final ThreadLocal<TransactionManager> MANAGERS = ThreadLocal.withInitial(TransactionManager::new);

    final Thread thread = Thread.currentThread();
    final List<Transaction> stack = Lists.newArrayList();
    final List<TransactionContext.RootCloseCallback>  rootCloseCallbacks = Lists.newArrayList();
    int currentDepth = -1;

    boolean isOpen() {
        return currentDepth > -1;
    }

    Transaction internalOpen(@Nullable TransactionContext parent) {
        if(parent == null) {
            if(isOpen())
                throw new IllegalStateException("A root transaction is already active on this thread " + thread);
        } else {
            var parentImpl = (Transaction) parent;
            parentImpl.validateCurrentTransaction();
            parentImpl.validateOpen();
        }

        return internalOpen();
    }

    private Transaction internalOpen() {
        Transaction current;

        if(stack.size() == ++currentDepth) {
            current = new Transaction(this, currentDepth);
            stack.add(current);
        } else {
            current = stack.get(currentDepth);
        }

        current.lifecycle = TransactionContext.Lifecycle.OPEN;
        return current;
    }

    TransactionContext.Lifecycle internalGetLifcecycle() {
        return isOpen() ? stack.get(currentDepth).lifecycle : TransactionContext.Lifecycle.NONE;
    }

    public static Transaction open(@Nullable TransactionContext parent) {
        return get().internalOpen(parent);
    }

    public static TransactionContext.Lifecycle getLifecycle() {
        return get().internalGetLifcecycle();
    }

    public static boolean isActive() {
        return getLifecycle() != TransactionContext.Lifecycle.NONE;
    }

    private static TransactionManager get() {
        return MANAGERS.get();
    }
}
