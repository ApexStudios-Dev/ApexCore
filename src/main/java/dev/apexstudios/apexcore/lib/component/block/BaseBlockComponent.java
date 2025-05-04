package dev.apexstudios.apexcore.lib.component.block;

import dev.apexstudios.apexcore.lib.component.BaseComponent;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
public class BaseBlockComponent extends BaseComponent<BlockComponent, Block> implements BlockComponent {
    protected BaseBlockComponent(ComponentHolder<BlockComponent, Block> holder) {
        super(holder);
    }
}
