package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.provider.tag.SimpleIntrusiveTagBuilder;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;

final class SimpleIntrusiveTagBuilderImpl<TRegistry> extends IntrusiveTagBuilderImpl<TRegistry, SimpleIntrusiveTagBuilder<TRegistry>> implements SimpleIntrusiveTagBuilder<TRegistry> {
    private final Function<TRegistry, ResourceKey<TRegistry>> keyLookup;

    SimpleIntrusiveTagBuilderImpl(String namespace, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        super(namespace);

        this.keyLookup = keyLookup;
    }

    @Override
    protected ResourceKey<TRegistry> getKey(TRegistry value) {
        return keyLookup.apply(value);
    }
}
