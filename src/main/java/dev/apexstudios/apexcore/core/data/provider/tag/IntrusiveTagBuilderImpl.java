package dev.apexstudios.apexcore.core.data.provider.tag;

import dev.apexstudios.apexcore.lib.data.provider.tag.IntrusiveTagBuilder;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;

final class IntrusiveTagBuilderImpl<TRegistry> extends TagBuilderImpl<TRegistry, IntrusiveTagBuilder<TRegistry>> implements IntrusiveTagBuilder<TRegistry> {
    private final Function<TRegistry, ResourceKey<TRegistry>> keyLookup;

    IntrusiveTagBuilderImpl(String namespace, Function<TRegistry, ResourceKey<TRegistry>> keyLookup) {
        super(namespace);

        this.keyLookup = keyLookup;
    }

    // region: Add
    // region: Element
    // region: Required
    @Override
    public IntrusiveTagBuilder<TRegistry> withElement(TRegistry value) {
        return withElement(keyLookup.apply(value));
    }
    // endregion

    // region: Optional
    @Override
    public IntrusiveTagBuilder<TRegistry> withOptionalElement(TRegistry value) {
        return withOptionalElement(keyLookup.apply(value));
    }
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    @Override
    public IntrusiveTagBuilder<TRegistry> removeElement(TRegistry value) {
        return removeElement(keyLookup.apply(value));
    }
    // endregion

    // region: Optional
    @Override
    public IntrusiveTagBuilder<TRegistry> removeOptionalElement(TRegistry value) {
        return removeOptionalElement(keyLookup.apply(value));
    }
    // endregion
    // endregion
    // endregion
}
