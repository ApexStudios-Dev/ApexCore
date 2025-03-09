package dev.apexstudios.apexcore.lib.data.provider.model;

import dev.apexstudios.apexcore.core.ApexCore;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.world.level.block.Block;

public interface ApexModelTemplates {
    TextureSlot SLOT_ALL_TINTED = TextureSlot.create("all_tinted", TextureSlot.ALL);

    ModelTemplate CUBE_ALL_TINTED = ModelTemplates.create(ApexCore.id("cube_all_tinted"), TextureSlot.ALL, SLOT_ALL_TINTED);

    interface Textured {
        TexturedModel.Provider CUBE_ALL_TINTED = TexturedModel.createDefault(Textured::cubeTinted, ApexModelTemplates.CUBE_ALL_TINTED);

        static TextureMapping cubeTinted(Block block) {
            var blockTexture = TextureMapping.getBlockTexture(block);
            return new TextureMapping().put(TextureSlot.ALL, blockTexture).put(SLOT_ALL_TINTED, blockTexture.withSuffix("_tint"));
        }
    }
}
