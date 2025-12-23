package dev.apexstudios.apexcore.common.data.provider.tag;

import dev.apexstudios.apexcore.api.data.provider.tag.SimpleTagBuilder;

final class SimpleTagBuilderImpl<TRegistry> extends TagBuilderImpl<TRegistry, SimpleTagBuilder<TRegistry>> implements SimpleTagBuilder<TRegistry> {
    SimpleTagBuilderImpl(String namespace) {
        super(namespace);
    }
}
