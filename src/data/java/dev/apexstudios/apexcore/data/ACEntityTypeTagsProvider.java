package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.common.seat.SeatSetup;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.neoforged.neoforge.common.Tags;

final class ACEntityTypeTagsProvider extends EntityTypeTagsProvider {
    ACEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ApexCore.ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(SeatSetup.ENTITY.getKey());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).add(SeatSetup.ENTITY.getKey());
    }
}
