package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.Optional;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public interface DelegatedAttachmentHolder extends IAttachmentHolder {
    IAttachmentHolder delegate();

    @Override
    default boolean hasAttachments() {
        return delegate().hasAttachments();
    }

    @Override
    default boolean hasData(AttachmentType<?> type) {
        return delegate().hasData(type);
    }

    @Override
    default <T> boolean hasData(Supplier<AttachmentType<T>> type) {
        return delegate().hasData(type);
    }

    @Override
    default <T> T getData(AttachmentType<T> type) {
        return delegate().getData(type);
    }

    @Override
    default <T> T getData(Supplier<AttachmentType<T>> type) {
        return delegate().getData(type);
    }

    @Override
    default <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return delegate().getExistingData(type);
    }

    @Override
    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return delegate().getExistingData(type);
    }

    @Override
    default <T> @Nullable T setData(AttachmentType<T> type, T data) {
        return delegate().setData(type, data);
    }

    @Override
    default <T> @Nullable T setData(Supplier<AttachmentType<T>> type, T data) {
        return delegate().setData(type, data);
    }

    @Override
    default <T> @Nullable T removeData(AttachmentType<T> type) {
        return delegate().removeData(type);
    }

    @Override
    default <T> @Nullable T removeData(Supplier<AttachmentType<T>> type) {
        return delegate().removeData(type);
    }
}
