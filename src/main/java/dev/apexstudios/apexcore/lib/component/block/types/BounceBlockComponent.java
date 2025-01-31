package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;

public final class BounceBlockComponent extends BaseBlockComponent {
    public static final ComponentType<BlockComponent, BounceBlockComponent, ComponentBuilder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("bounce"),
            BounceBlockComponent::new
    );

    private BounceBlockComponent(ComponentHolder<BlockComponent> holder) {
        super(holder);
    }

    @Override
    public boolean updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if(entity.isSuppressingBounce())
            return super.updateEntityMovementAfterFallOn(level, entity);

        var delta = entity.getDeltaMovement();

        if(delta.y < 0D) {
            var amount = entity instanceof LivingEntity ? 1D : .8D;
            entity.setDeltaMovement(delta.x, -delta.y * .66F * amount, delta.z);
        }

        return true;
    }
}
