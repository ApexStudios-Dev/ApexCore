package dev.apexstudios.apexcore.lib.transfer.transaction;

import com.google.common.util.concurrent.Runnables;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SimpleSnapshotJournal<T> extends SnapshotJournal<T> {
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Runnable changeListener;

    public SimpleSnapshotJournal(Supplier<T> getter, Consumer<T> setter, Runnable changeListener) {
        this.getter = getter;
        this.setter = setter;
        this.changeListener = changeListener;
    }

    public SimpleSnapshotJournal(Supplier<T> getter, Consumer<T> setter) {
        this(getter, setter, Runnables.doNothing());
    }

    @Override
    protected T createSnapshot() {
        return getter.get();
    }

    @Override
    protected void revertToSnapshot(T snapshot) {
        setter.accept(snapshot);
    }

    @Override
    protected void onCommit(T originalState) {
        super.onCommit(originalState);
        changeListener.run();
    }
}
