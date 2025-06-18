package dev.apexstudios.apexcore.lib.transfer.transaction;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public abstract class SnapshotJournal<T> implements TransactionContext.CloseCallback, TransactionContext.RootCloseCallback {
    private static int DEEPEST_LAYER = -1;
    private static SnapshotJournal<?> DEEPEST_SNAPSHOT = null;

    private final ArrayList<T> snapshots = Lists.newArrayList();
    @Nullable private T originalState = null;

    protected abstract T createSnapshot();

    protected abstract void revertToSnapshot(T snapshot);

    protected void releaseSnapshot(T snapshot) {

    }

    protected void onCommit(T originalState) {

    }

    public void updateSnapshots(TransactionContext transaction) {
        var nestingDepth = transaction.nestingDepth();
        snapshots.ensureCapacity(nestingDepth);

        for(var i = snapshots.size(); i <= nestingDepth; i++) {
            snapshots.add(null);
        }

        if(snapshots.get(nestingDepth) == null) {
            var snapshot = Objects.requireNonNull(createSnapshot(), "Snapshot may not be null");
            snapshots.set(nestingDepth, snapshot);
            transaction.addCloseCallback(this);
        }
    }

    @Override
    public void onClose(TransactionContext transaction, TransactionContext.Result result) {
        var max = Math.max(DEEPEST_LAYER, transaction.nestingDepth());

        if(max != DEEPEST_LAYER) {
            DEEPEST_LAYER = max;
            DEEPEST_SNAPSHOT = this;
        }

        var snapshot = snapshots.remove(transaction.nestingDepth());

        if(result.wasAborted()) {
            revertToSnapshot(snapshot);
            releaseSnapshot(snapshot);
            return;
        }

        if(transaction.nestingDepth() <= 0) {
            originalState = snapshot;
            transaction.addRootCloseCallback(this);
            return;
        }

        if(snapshots.get(transaction.nestingDepth() - 1) == null) {
            snapshots.set(transaction.nestingDepth() - 1, snapshot);
            transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
        } else {
            releaseSnapshot(snapshot);
        }
    }

    @Override
    public void afterRootClose(TransactionContext.Result result) {
        Objects.requireNonNull(originalState);

        onCommit(originalState);
        releaseSnapshot(originalState);
        originalState = null;
    }

    public static int getDeepestLayer() {
        return DEEPEST_LAYER;
    }

    public static String getDeepestSnapshot() {
        return DEEPEST_SNAPSHOT == null ? "Nothing" : DEEPEST_SNAPSHOT.getClass().toString();
    }
}
