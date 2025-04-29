package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;

public final class BounceBlockComponent extends BaseBlockComponent {
    public static final BlockComponentType<BounceBlockComponent, Object> COMPONENT_TYPE = BlockComponentType.register(
            ApexCore.identifier("bounce"),
            BounceBlockComponent::new
    );

    private BounceBlockComponent(BlockComponentHolder holder) {
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
