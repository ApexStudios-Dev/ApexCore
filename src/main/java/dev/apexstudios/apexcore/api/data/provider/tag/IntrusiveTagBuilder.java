package dev.apexstudios.apexcore.api.data.provider.tag;

import net.minecraft.core.Holder;

public interface IntrusiveTagBuilder<TRegistry, TBuilder extends IntrusiveTagBuilder<TRegistry, TBuilder>> extends TagBuilder<TRegistry, TBuilder> {
    // region: Add
    // region: Element
    // region: Required
    TBuilder withElement(TRegistry value);

    @Override
    default TBuilder withElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::withElement, this::withElement);
    }
    // endregion

    // region: Optional
    TBuilder withOptionalElement(TRegistry value);

    @Override
    default TBuilder withOptionalElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::withOptionalElement, this::withOptionalElement);
    }
    // endregion
    // endregion
    // endregion

    // region: Remove
    // region: Element
    // region: Required
    TBuilder removeElement(TRegistry value);

    default TBuilder removeElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::removeElement, this::removeElement);
    }
    // endregion

    // region: Optional
    TBuilder removeOptionalElement(TRegistry value);

    default TBuilder removeOptionalElement(Holder<TRegistry> holder) {
        return holder.unwrap().map(this::removeOptionalElement, this::removeOptionalElement);
    }
    // endregion
    // endregion
    // endregion
}
