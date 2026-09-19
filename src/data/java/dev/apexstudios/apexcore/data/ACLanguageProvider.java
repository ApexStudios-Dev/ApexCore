package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.apexcore.common.seat.SeatSetup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

final class ACLanguageProvider extends LanguageProvider {
    ACLanguageProvider(PackOutput output) {
        super(output, ApexCore.ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addEntityType(SeatSetup.ENTITY, "Seat");
    }
}
