package dev.apexstudios.apexcore.lib.transfer.transaction;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TransactionContext {
    TransactionContext ROOT = null;

    int nestingDepth();

    Transaction getOpenTransaction(int nestingDepth);

    void addCloseCallback(CloseCallback callback);

    void addRootCloseCallback(RootCloseCallback callback);

    @FunctionalInterface
    interface CloseCallback {
        void onClose(TransactionContext transaction, Result result);
    }

    @FunctionalInterface
    interface RootCloseCallback {
        void afterRootClose(Result result);
    }

    enum Result {
        ABORTED,
        COMMITTED;

        public boolean wasAborted() {
            return this == ABORTED;
        }

        public boolean wasComitted() {
            return this == COMMITTED;
        }
    }

    enum Lifecycle {
        NONE,
        OPEN,
        CLOSING,
        ROOT_CLOSING
    }
}
