package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.BaseComponent;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;

public class BaseBlockComponent extends BaseComponent<BlockComponent> implements BlockComponent {
    protected BaseBlockComponent(ComponentHolder<BlockComponent> holder) {
        super(holder);
    }
}
