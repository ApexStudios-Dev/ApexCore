package dev.apexstudios.apexcore.core.data.provider.tag;

import dev.apexstudios.apexcore.lib.data.provider.tag.SimpleTagBuilder;

final class SimpleTagBuilderImpl<TRegistry> extends TagBuilderImpl<TRegistry, SimpleTagBuilder<TRegistry>> implements SimpleTagBuilder<TRegistry> {
    SimpleTagBuilderImpl(String namespace) {
        super(namespace);
    }
}
