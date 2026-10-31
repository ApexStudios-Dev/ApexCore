package dev.apexstudios.apexcore.api;

import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredEntityType;
import dev.apexstudios.registree.api.util.EntityRegistrations;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;

public interface ApexCore {
    String ID = "apexcore";
    Registree REGISTREE = Registree.create(ID);

    DeferredEntityType<FakeItemEntity> FAKE_ITEM = EntityRegistrations.register(
            REGISTREE,
            "fake_item",
            FakeItemEntity::new,
            MobCategory.MISC,
            builder -> builder
                    .noLootTable()
                    .sized(.25F, .25F)
                    .eyeHeight(ItemEntity.EYE_HEIGHT)
                    .clientTrackingRange(6)
                    .updateInterval(SharedConstants.TICKS_PER_SECOND)
    );
}
