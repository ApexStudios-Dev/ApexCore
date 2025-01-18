package dev.apexstudios.apexcore.lib.data.provider;

import dev.apexstudios.apexcore.core.data.provider.LanguageProviderImpl;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

public interface LanguageProvider {
    ProviderType<LanguageProvider> PROVIDER_TYPE = LanguageProviderImpl.PROVIDER_TYPE;

    LanguageProvider add(String key, String value);

    default LanguageProvider add(String key, String value, String descriptionValue) {
        return add(key, value).add(key + ".description", descriptionValue);
    }

    default LanguageProvider addBlock(Supplier<? extends Block> key, String name) {
        return add(key.get(), name);
    }

    default LanguageProvider add(Block key, String name) {
        return add(key.getDescriptionId(), name);
    }

    default LanguageProvider addItem(Supplier<? extends Item> key, String name) {
        return add(key.get(), name);
    }

    default LanguageProvider add(Item key, String name) {
        return add(key.getDescriptionId(), name);
    }

    default LanguageProvider addEntityType(Supplier<? extends EntityType<?>> key, String name) {
        return add(key.get(), name);
    }

    default LanguageProvider add(EntityType<?> key, String name) {
        return add(key.getDescriptionId(), name);
    }

    default LanguageProvider addTag(Supplier<? extends TagKey<?>> key, String name) {
        return add(key.get(), name);
    }

    default LanguageProvider add(TagKey<?> tagKey, String name) {
        return add(Tags.getTagTranslationKey(tagKey), name);
    }

    default LanguageProvider add(ResourceKey<?> registyrKey, String translationPrefix, String value) {
        return add(registyrKey.location().toLanguageKey(translationPrefix), value);
    }

    default LanguageProvider addDimension(ResourceKey<Level> dimension, String value) {
        return add(dimension, ILevelExtension.TRANSLATION_PREFIX, value);
    }

    default LanguageProvider addCreativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, String value) {
        return add(creativeModeTab, "itemGroup", value);
    }

    default LanguageProvider add(GameRules.Key<?> gameRule, String value, String descriptionValue) {
        return add(gameRule.getDescriptionId(), value, descriptionValue);
    }
}
