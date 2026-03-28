package dev.apexstudios.apexcore.neoforge.api.data.provider.model;

import dev.apexstudios.apexcore.xplat.common.ApexCoreXplat;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.world.level.block.Block;

public interface ApexModelTemplates {
    TextureSlot SLOT_ALL_TINTED = TextureSlot.create("all_tinted", TextureSlot.ALL);
    TextureSlot SLOT_WOOL_TINTED = TextureSlot.create("wool_tinted", TextureSlot.WOOL);

    ModelTemplate CUBE_ALL_TINTED = ModelTemplates.create(ApexCoreXplat.id("cube_all_tinted"), TextureSlot.ALL, SLOT_ALL_TINTED);
    ModelTemplate CARPET_TINTED = ModelTemplates.create(ApexCoreXplat.id("carpet_tinted"), TextureSlot.WOOL, SLOT_WOOL_TINTED);

    interface Textured {
        TexturedModel.Provider CUBE_ALL_TINTED = TexturedModel.createDefault(Textured::cubeTinted, ApexModelTemplates.CUBE_ALL_TINTED);
        TexturedModel.Provider CARPET_TINTED = TexturedModel.createDefault(Textured::woolTinted, ApexModelTemplates.CARPET_TINTED);

        static TextureMapping cubeTinted(Block block) {
            return new TextureMapping()
                    .put(TextureSlot.ALL, TextureMapping.getBlockTexture(block))
                    .put(SLOT_ALL_TINTED, TextureMapping.getBlockTexture(block, "_tint"));
        }

        static TextureMapping woolTinted(Block block) {
            return new TextureMapping()
                    .put(TextureSlot.WOOL, TextureMapping.getBlockTexture(block))
                    .put(SLOT_WOOL_TINTED, TextureMapping.getBlockTexture(block, "_tint"));
        }
    }
}
