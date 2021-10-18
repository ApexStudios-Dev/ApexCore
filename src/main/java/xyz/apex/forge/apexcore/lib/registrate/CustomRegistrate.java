package xyz.apex.forge.apexcore.lib.registrate;

import com.mojang.serialization.Codec;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.apex.forge.apexcore.lib.registrate.builders.PointOfInterestTypeBuilder;
import xyz.apex.forge.apexcore.lib.registrate.builders.StructureBuilder;
import xyz.apex.forge.apexcore.lib.registrate.builders.VillagerProfessionBuilder;

public class CustomRegistrate<R extends CustomRegistrate<R>> extends AbstractRegistrate<R>
{
	protected CustomRegistrate(String modId)
	{
		super(modId);
	}

	/*
		Structure Support
		Adapted from https://github.com/TelepathicGrunt/StructureTutorialMod/tree/1.16.3-Forge-jigsaw
	 */
	public <T extends Structure<NoFeatureConfig>> StructureBuilder<T, R, NoFeatureConfig> structure(StructureBuilder.StructureFactory<T, NoFeatureConfig> factory)
	{
		return structure(self(), currentName(), factory);
	}

	public <T extends Structure<NoFeatureConfig>> StructureBuilder<T, R, NoFeatureConfig> structure(String name, StructureBuilder.StructureFactory<T, NoFeatureConfig> factory)
	{
		return structure(self(), name, factory);
	}

	public <T extends Structure<NoFeatureConfig>, P> StructureBuilder<T, P, NoFeatureConfig> structure(P parent, StructureBuilder.StructureFactory<T, NoFeatureConfig> factory)
	{
		return structure(parent, currentName(), factory);
	}

	public <T extends Structure<NoFeatureConfig>, P> StructureBuilder<T, P, NoFeatureConfig> structure(P parent, String name, StructureBuilder.StructureFactory<T, NoFeatureConfig> factory)
	{
		return structure(parent, name, factory, () -> NoFeatureConfig.CODEC, () -> IFeatureConfig.NONE);
	}

	public <T extends Structure<C>, C extends IFeatureConfig> StructureBuilder<T, R, C> structure(StructureBuilder.StructureFactory<T, C> factory, NonNullSupplier<Codec<C>> codecSupplier, NonNullSupplier<C> configSupplier)
	{
		return structure(self(), currentName(), factory, codecSupplier, configSupplier);
	}

	public <T extends Structure<C>, C extends IFeatureConfig> StructureBuilder<T, R, C> structure(String name, StructureBuilder.StructureFactory<T, C> factory, NonNullSupplier<Codec<C>> codecSupplier, NonNullSupplier<C> configSupplier)
	{
		return structure(self(), name, factory, codecSupplier, configSupplier);
	}

	public <T extends Structure<C>, P, C extends IFeatureConfig> StructureBuilder<T, P, C> structure(P parent, StructureBuilder.StructureFactory<T, C> factory, NonNullSupplier<Codec<C>> codecSupplier, NonNullSupplier<C> configSupplier)
	{
		return structure(parent, currentName(), factory, codecSupplier, configSupplier);
	}

	public <T extends Structure<C>, P, C extends IFeatureConfig> StructureBuilder<T, P, C> structure(P parent, String name, StructureBuilder.StructureFactory<T, C> factory, NonNullSupplier<Codec<C>> codecSupplier, NonNullSupplier<C> configSupplier)
	{
		return entry(name, callback -> StructureBuilder.create(self(), parent, name, callback, factory, codecSupplier, configSupplier));
	}

	// Point of Interest Types
	public PointOfInterestTypeBuilder<PointOfInterestType, R> pointOfInterestType()
	{
		return pointOfInterestType(self(), currentName(), PointOfInterestType::new);
	}

	public <T extends PointOfInterestType> PointOfInterestTypeBuilder<T, R> pointOfInterestType(PointOfInterestTypeBuilder.PointOfInterestTypeFactory<T> factory)
	{
		return pointOfInterestType(self(), currentName(), factory);
	}

	public PointOfInterestTypeBuilder<PointOfInterestType, R> pointOfInterestType(String name)
	{
		return pointOfInterestType(self(), name, PointOfInterestType::new);
	}

	public <T extends PointOfInterestType> PointOfInterestTypeBuilder<T, R> pointOfInterestType(String name, PointOfInterestTypeBuilder.PointOfInterestTypeFactory<T> factory)
	{
		return pointOfInterestType(self(), name, factory);
	}

	public <P> PointOfInterestTypeBuilder<PointOfInterestType, P> pointOfInterestType(P parent)
	{
		return pointOfInterestType(parent, currentName(), PointOfInterestType::new);
	}

	public <T extends PointOfInterestType, P> PointOfInterestTypeBuilder<T, P> pointOfInterestType(P parent, PointOfInterestTypeBuilder.PointOfInterestTypeFactory<T> factory)
	{
		return pointOfInterestType(parent, currentName(), factory);
	}

	public <P> PointOfInterestTypeBuilder<PointOfInterestType, P> pointOfInterestType(P parent, String name)
	{
		return pointOfInterestType(parent, name, PointOfInterestType::new);
	}

	public <T extends PointOfInterestType, P> PointOfInterestTypeBuilder<T, P> pointOfInterestType(P parent, String name, PointOfInterestTypeBuilder.PointOfInterestTypeFactory<T> factory)
	{
		return entry(name, callback -> PointOfInterestTypeBuilder.create(self(), parent, name, callback, factory));
	}

	// Villager Professions
	public VillagerProfessionBuilder<VillagerProfession, R> villagerProfession()
	{
		return villagerProfession(self(), currentName(), VillagerProfession::new);
	}

	public <T extends VillagerProfession> VillagerProfessionBuilder<T, R> villagerProfession(VillagerProfessionBuilder.VillagerProfessionFactory<T> factory)
	{
		return villagerProfession(self(), currentName(), factory);
	}

	public VillagerProfessionBuilder<VillagerProfession, R> villagerProfession(String name)
	{
		return villagerProfession(self(), name, VillagerProfession::new);
	}

	public <T extends VillagerProfession> VillagerProfessionBuilder<T, R> villagerProfession(String name, VillagerProfessionBuilder.VillagerProfessionFactory<T> factory)
	{
		return villagerProfession(self(), name, factory);
	}

	public <P> VillagerProfessionBuilder<VillagerProfession, P> villagerProfession(P parent)
	{
		return villagerProfession(parent, currentName(), VillagerProfession::new);
	}

	public <T extends VillagerProfession, P> VillagerProfessionBuilder<T, P> villagerProfession(P parent, VillagerProfessionBuilder.VillagerProfessionFactory<T> factory)
	{
		return villagerProfession(parent, currentName(), factory);
	}

	public <P> VillagerProfessionBuilder<VillagerProfession, P> villagerProfession(P parent, String name)
	{
		return villagerProfession(parent, name, VillagerProfession::new);
	}

	public <T extends VillagerProfession, P> VillagerProfessionBuilder<T, P> villagerProfession(P parent, String name, VillagerProfessionBuilder.VillagerProfessionFactory<T> factory)
	{
		return entry(name, callback -> VillagerProfessionBuilder.create(self(), parent, name, callback, factory));
	}

	public static <R extends CustomRegistrate<R>> NonNullLazyValue<R> create(String modId, NonNullFunction<String, R> builder)
	{
		return new NonNullLazyValue<>(() -> builder.apply(modId).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus()));
	}
}