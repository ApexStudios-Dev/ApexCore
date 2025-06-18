package dev.apexstudios.apexcore.lib.transfer.resource;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

public interface IRegisteredResource<V> extends IResource {
    default V value() {
        return holder().value();
    }

    Holder<V> holder();

    default boolean is(TagKey<V> tag) {
        return holder().is(tag);
    }

    default boolean is(V value) {
        return value() == value;
    }

    default boolean is(Predicate<Holder<V>> test) {
        return test.test(holder());
    }

    default boolean is(Holder<V> holder) {
        return holder().is(holder);
    }

    default boolean is(HolderSet<V> holders) {
        return holders.contains(holder());
    }
}
