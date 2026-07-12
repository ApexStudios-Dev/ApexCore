package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.provider.tag.IntrusiveTagBuilder;
import net.minecraft.resources.ResourceKey;

public sealed abstract class IntrusiveTagBuilderImpl<TRegistry, TSelf extends IntrusiveTagBuilder<TRegistry, TSelf>> extends TagBuilderImpl<TRegistry, TSelf> implements IntrusiveTagBuilder<TRegistry, TSelf> permits BlockTagBuilderImpl, ItemTagBuilderImpl, SimpleIntrusiveTagBuilderImpl {
    protected IntrusiveTagBuilderImpl(String namespace) {
        super(namespace);
    }

    protected abstract ResourceKey<TRegistry> getKey(TRegistry value);

    // region: Add
    // region: Element
    // region: Required
    @Override
    public TSelf withElement(TRegistry value) {
        return withElement(getKey(value));
    }
    // endregion

    // region: Optional
    @Override
    public TSelf withOptionalElement(TRegistry value) {
        return withOptionalElement(getKey(value));
    }
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    @Override
    public TSelf removeElement(TRegistry value) {
        return removeElement(getKey(value));
    }
    // endregion

    // region: Optional
    @Override
    public TSelf removeOptionalElement(TRegistry value) {
        return removeOptionalElement(getKey(value));
    }
    // endregion
    // endregion
    // endregion
}
