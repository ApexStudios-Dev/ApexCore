package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.api.ApexCore;
import dev.apexstudios.apexcore.api.data.LanguageProvider;
import net.minecraft.data.PackOutput;

final class LangProvider extends LanguageProvider {
    LangProvider(PackOutput output) {
        super(output, ApexCore.ID);
    }

    @Override
    protected void addTranslations() {
        addTranslatableComponent(ApexCoreDataEntryPoint.PACK_DESC, "ApexCore resources");
    }
}
