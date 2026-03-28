package dev.apexstudios.apexcore.neoforge.api.data;

import dev.apexstudios.apexcore.neoforge.api.data.provider.LanguageProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.ParticleProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.RecipeProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.datamap.DataMapProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.loot.LootTableProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.model.ModelProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.tag.IntrusiveTagProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.tag.SimpleTagProvider;
import dev.apexstudios.apexcore.neoforge.api.data.provider.tag.TagProvider;
import dev.apexstudios.apexcore.neoforge.common.data.provider.ModelProviderImpl;
import dev.apexstudios.apexcore.xplat.common.ApexCoreXplat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;

public interface ProviderTypes {
    ProviderType<LanguageProvider> LANGUAGE = LanguageProvider.PROVIDER_TYPE;
    ProviderType<ModelProvider> MODELS = ProviderType.registerForDist(ApexCoreXplat.identifier("models"), Dist.CLIENT, () -> ModelProviderImpl::new);
    ProviderType<RecipeProvider> RECIPES = RecipeProvider.PROVIDER_TYPE;
    ProviderType<ParticleProvider> PARTICLES = ParticleProvider.PROVIDER_TYPE;

    ProviderType<IntrusiveTagProvider<Item>> ITEM_TAGS = TagProvider.registerIntrusiveForHolder(ApexCoreXplat.ID, Registries.ITEM, Item::builtInRegistryHolder);
    ProviderType<IntrusiveTagProvider<Block>> BLOCK_TAGS = TagProvider.registerIntrusiveForHolder(ApexCoreXplat.ID, Registries.BLOCK, Block::builtInRegistryHolder);
    ProviderType<IntrusiveTagProvider<EntityType<?>>> ENTITY_TYPE_TAGS = TagProvider.registerIntrusiveForHolder(ApexCoreXplat.ID, Registries.ENTITY_TYPE, EntityType::builtInRegistryHolder);
    ProviderType<IntrusiveTagProvider<Fluid>> FLUID_TAGS = TagProvider.registerIntrusiveForHolder(ApexCoreXplat.ID, Registries.FLUID, Fluid::builtInRegistryHolder);

    ProviderType<SimpleTagProvider<Enchantment>> ENCHANTMENT_TAGS = TagProvider.registerSimple(ApexCoreXplat.ID, Registries.ENCHANTMENT);
    ProviderType<SimpleTagProvider<Biome>> BIOME_TAGS = TagProvider.registerSimple(ApexCoreXplat.ID, Registries.BIOME);
    ProviderType<SimpleTagProvider<Structure>> STRUCTURE_TAGS = TagProvider.registerSimple(ApexCoreXplat.ID, Registries.STRUCTURE);
    ProviderType<SimpleTagProvider<DamageType>> DAMAGE_TYPE_TAGS = TagProvider.registerSimple(ApexCoreXplat.ID, Registries.DAMAGE_TYPE);
    ProviderType<SimpleTagProvider<Potion>> POTION_TAGS = TagProvider.registerSimple(ApexCoreXplat.ID, Registries.POTION);

    ProviderType<DataMapProvider> DATA_MAP = DataMapProvider.PROVIDER_TYPE;
    ProviderType<LootTableProvider> LOOT_TABLE = LootTableProvider.PROVIDER_TYPE;
}
