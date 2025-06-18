package dev.apexstudios.apexcore.lib.transfer.transaction;

import com.google.common.collect.Lists;
import java.util.List;

public final class Transaction implements AutoCloseable, TransactionContext {
    Lifecycle lifecycle = Lifecycle.NONE;
    private final TransactionManager manager;
    private final int nestingDepth;
    private final List<CloseCallback> closeCallbacks = Lists.newArrayList();

    Transaction(TransactionManager manager, int nestingDepth) {
        this.manager = manager;
        this.nestingDepth = nestingDepth;
    }

    public void commit() {
        close(Result.COMMITTED);
    }

    @Override
    public int nestingDepth() {
        validateCurrentThread();
        return nestingDepth;
    }

    @Override
    public Transaction getOpenTransaction(int nestingDepth) {
        validateCurrentThread();

        if(nestingDepth < 0)
            throw new IndexOutOfBoundsException("Nesting depth may not be negative");
        if(nestingDepth > manager.currentDepth)
            throw new IndexOutOfBoundsException("There is no open transaction for nesting depth " + nestingDepth);

        var transaction = manager.stack.get(nestingDepth);
        transaction.validateOpen();
        return transaction;
    }

    @Override
    public void addCloseCallback(CloseCallback callback) {
        validateCurrentThread();
        validateOpen();
        closeCallbacks.add(callback);
    }

    @Override
    public void addRootCloseCallback(RootCloseCallback callback) {
        validateCurrentThread();

        if(!manager.isOpen())
            throw new IllegalStateException("There is no open transaction on this thread");

        manager.rootCloseCallbacks.add(callback);
    }

    @Override
    public void close() {
        if(manager.isOpen() && lifecycle == Lifecycle.OPEN)
            close(Result.ABORTED);
    }

    @Override
    public String toString() {
        return "Transaction[depth=" + nestingDepth + ", lifecycle=" + lifecycle.name() + ", thread=" + manager.thread.getName() + ']';
    }

    private void validateCurrentThread() {
        if(Thread.currentThread() != manager.thread)
            throw new IllegalStateException("Attempted to access transaction state from thread " + Thread.currentThread().getName() + ", but this transaction is only valid on thread " + manager.thread.getName());
    }

    void validateCurrentTransaction() {
        validateCurrentThread();

        if(manager.isOpen() && manager.stack.get(manager.currentDepth) == this)
            return;

        throw new IllegalStateException("Transaction function was called on a transaction with depth " + nestingDepth + ", but the current transaction has depth " + manager.currentDepth);
    }

    void validateOpen() {
        if(lifecycle != Lifecycle.OPEN)
            throw new IllegalStateException("Transaction operation cannot be applied to a closed transaction");
    }

    void close(Result result) {
        validateCurrentTransaction();
        validateOpen();
        lifecycle = Lifecycle.CLOSING;
        RuntimeException closeException = null;

        for(var i = closeCallbacks.size() - 1; i >= 0; i--) {
            try {
                closeCallbacks.get(i).onClose(this, result);
            } catch (Exception e) {
                if(closeException == null)
                    closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback", e);
                else
                    closeException.addSuppressed(e);
            }
        }

        closeCallbacks.clear();

        if(manager.currentDepth == 0) {
            lifecycle = Lifecycle.ROOT_CLOSING;

            for(var i = manager.rootCloseCallbacks.size() - 1; i >= 0; i--) {
                try {
                    manager.rootCloseCallbacks.get(i).afterRootClose(result);
                } catch (Exception e) {
                    if(closeException == null)
                        closeException = new RuntimeException("Encountered an exception while invoking a transaction root close callback", e);
                    else
                        closeException.addSuppressed(e);
                }
            }

            manager.rootCloseCallbacks.clear();
        }

        manager.currentDepth--;
        lifecycle = Lifecycle.NONE;

        if(closeException != null)
            throw closeException;
    }
}
