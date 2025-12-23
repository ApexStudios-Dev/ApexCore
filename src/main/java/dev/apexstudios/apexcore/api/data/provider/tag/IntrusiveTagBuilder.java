package dev.apexstudios.apexcore.api.data.provider.tag;

import net.minecraft.core.Holder;

public interface IntrusiveTagBuilder<TRegistry> extends TagBuilder<TRegistry, IntrusiveTagBuilder<TRegistry>> {
    // region: Add
    // region: Element
    // region: Required
    IntrusiveTagBuilder<TRegistry> withElement(TRegistry value);

    @Override
    default IntrusiveTagBuilder<TRegistry> withElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::withElement, this::withElement);
    }
    // endregion

    // region: Optional
    IntrusiveTagBuilder<TRegistry> withOptionalElement(TRegistry value);

    @Override
    default IntrusiveTagBuilder<TRegistry> withOptionalElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::withOptionalElement, this::withOptionalElement);
    }
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    IntrusiveTagBuilder<TRegistry> removeElement(TRegistry value);

    default IntrusiveTagBuilder<TRegistry> removeElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::removeElement, this::removeElement);
    }
    // endregion

    // region: Optional
    IntrusiveTagBuilder<TRegistry> removeOptionalElement(TRegistry value);

    default IntrusiveTagBuilder<TRegistry> removeOptionalElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::removeOptionalElement, this::removeOptionalElement);
    }
    // endregion
    // endregion
    // endregion
}
