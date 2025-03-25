package dev.apexstudios.apexcore.lib.component.block.entity;

import dev.apexstudios.apexcore.lib.component.BaseComponent;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BaseBlockEntityComponent extends BaseComponent<BlockEntityComponent, BlockEntity> implements BlockEntityComponent {
    protected BaseBlockEntityComponent(ComponentHolder<BlockEntityComponent, BlockEntity> holder) {
        super(holder);
    }
}
