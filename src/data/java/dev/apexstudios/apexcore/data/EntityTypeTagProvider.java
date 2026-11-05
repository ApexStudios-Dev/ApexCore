package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.api.ApexCore;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;

final class EntityTypeTagProvider extends TagsProvider<EntityType<?>> {
    EntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, ApexCore.ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(ApexCore.FAKE_ITEM.key());
    }
}
