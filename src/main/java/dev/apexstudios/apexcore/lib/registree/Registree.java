package dev.apexstudios.apexcore.lib.registree;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import dev.apexstudios.apexcore.lib.registree.holder.ApexDeferredHolder;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredAttachment;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredBlock;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredBlockEntity;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredDataComponent;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredEntity;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredEntityDataSerializer;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredFluid;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredFluidType;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredItem;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredMenu;
import dev.apexstudios.apexcore.lib.registree.holder.DeferredRecipeSerializer;
import dev.apexstudios.apexcore.lib.registree.type.SimpleRecipeSerializer;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.Nullable;

public class Registree {
    protected final String namespace;

    private final Table<ResourceKey<? extends Registry<?>>, String, Holder.Reference<?>> holders = HashBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, Function<ResourceLocation, ?>> factories = HashBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, Consumer<?>> listeners = HashBasedTable.create();
    private final Set<ResourceKey<? extends Registry<?>>> registered = Sets.newHashSet();
    private final Set<ResourceKey<? extends Registry<?>>> finalized = Sets.newHashSet();
    private boolean frozen = false;
    @Nullable private IEventBus modBus = null;
    private Consumer<IEventBus> delayedEventRegistration = Consumers.nop();

    public Registree(String namespace) {
        this.namespace = namespace;
    }

    public void registerEvents(IEventBus modBus) {
        if(this.modBus != null)
            return;

        modBus.addListener(RegisterEvent.class, event -> register(event.getRegistry()));
        modBus.addListener(EventPriority.LOW, RegisterEvent.class, event -> invokeListeners(event.getRegistry()));
        modBus.addListener(EventPriority.LOWEST, RegisterEvent.class, event -> frozen = true);
        delayedEventRegistration.accept(modBus);
        delayedEventRegistration = null;
        this.modBus = modBus;
    }

    private void withEventBus(Consumer<IEventBus> consumer) {
        if(modBus == null)
            delayedEventRegistration = delayedEventRegistration.andThen(consumer);
        else
            consumer.accept(modBus);
    }

    private <TRegistry> void register(Registry<TRegistry> registry) {
        var registryType = registry.key();

        if(!registered.add(registryType))
            throw new IllegalStateException("Duplicate registry registration: " + namespace + '#' + registryType.registry());

        factories.row(registryType).forEach((registryName, factory) -> {
            var fullName = registryName(registryName);
            var holder = Registry.registerForHolder(registry, fullName, (TRegistry) factory.apply(fullName));
            holders.put(registryType, registryName, holder);
        });
    }

    private <TRegistry> void invokeListeners(Registry<TRegistry> registry) {
        var registryType = registry.key();

        if(!registered.contains(registryType))
            throw new IllegalStateException("Can not finalize registry before elements are registered: " + namespace + '#' + registryType.registry());
        if(!finalized.add(registryType))
            throw new IllegalStateException("Duplicate registry finalization: " + namespace + '#' + registryType.registry());

        listeners.row(registryType).forEach((registryName, listener) -> {
            getOptional(registryType, registryName).ifPresent((Consumer<? super TRegistry>) listener);
        });
    }

    public final String namespace() {
        return namespace;
    }

    public final ResourceLocation registryName(String registryName) {
        return ResourceLocation.fromNamespaceAndPath(namespace(), registryName);
    }

    public final <TRegistry> ResourceKey<TRegistry> registryKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return ResourceKey.create(registryType, registryName(registryName));
    }

    public final <TRegistry> TagKey<TRegistry> tag(ResourceKey<? extends Registry<TRegistry>> registryType, String tagPath) {
        return TagKey.create(registryType, registryName(tagPath));
    }

    public final Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
        return holders.rowKeySet().stream();
    }

    // region: Registry
    public final <TRegistry> Optional<Holder.Reference<TRegistry>> get(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Optional.of((Holder.Reference<TRegistry>) holders.get(registryType, registryName));
    }

    public final <TRegistry> Holder.Reference<TRegistry> getOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).orElseThrow(() -> new NoSuchElementException("Missing key in '" + registryType.registry() + "': '" + namespace() + ':' + registryName + "'"));
    }

    @Nullable
    public final <TRegistry> TRegistry getValue(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).map(Holder::value).orElse(null);
    }

    public final <TRegistry> Optional<TRegistry> getOptional(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).map(Holder::value);
    }

    public final <TRegistry> TRegistry getValueOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return getOrThrow(registryType, registryName).value();
    }

    public final <TRegistry> Stream<Holder.Reference<TRegistry>> listElements(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return holders.row(registryType).values().stream().map(holder -> (Holder.Reference<TRegistry>) holder);
    }

    public final <TRegistry> Stream<TRegistry> stream(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return listElements(registryType).map(Holder::value);
    }

    public final <TRegistry> boolean containsKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return holders.contains(registryType, registryName) || factories.contains(registryType, registryName);
    }

    public final <TRegistry> void listenFor(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Consumer<? super TRegistry> listener) {
        if(finalized.contains(registryType))
            getOptional(registryType, registryName).ifPresent(listener);
        else
            listeners.put(registryType, registryName, listener);
    }

    public final boolean isRegistered(ResourceKey<? extends Registry<?>> registryType) {
        return frozen || registered.contains(registryType) || finalized.contains(registryType);
    }

    public final boolean isRegistered() {
        return frozen;
    }

    public final <TRegistry> HolderLookup.RegistryLookup<TRegistry> asLookup(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return new HolderLookup.RegistryLookup<>() {
            @Override
            public ResourceKey<? extends Registry<? extends TRegistry>> key() {
                return registryType;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return Lifecycle.stable();
            }

            @Override
            public Stream<Holder.Reference<TRegistry>> listElements() {
                return Registree.this.listElements(registryType);
            }

            @Override
            public Stream<HolderSet.Named<TRegistry>> listTags() {
                return Stream.empty();
            }

            @Override
            public Optional<Holder.Reference<TRegistry>> get(ResourceKey<TRegistry> registryKey) {
                return registryKey.isFor(registryType) && registryKey.location().getNamespace().equals(namespace) ? Registree.this.get(registryType, registryKey.location().getPath()) : Optional.empty();
            }

            @Override
            public Optional<HolderSet.Named<TRegistry>> get(TagKey<TRegistry> tag) {
                return Optional.empty();
            }

            @Override
            public boolean canSerializeIn(HolderOwner<TRegistry> owner) {
                return false;
            }
        };
    }
    // endregion

    // region: Registrar
    // region: Generic
    public final <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> factory) {
        if(registered.contains(registryType))
            throw new IllegalStateException("Registree is already frozen: " + namespace + '#' + registryType.registry());
        if(factories.put(registryType, registryName, factory) != null)
            throw new IllegalStateException("Duplicate registration: " + registryName + " in registry: " + namespace + '#' + registryType.registry());

        return registryKey(registryType, registryName);
    }

    public final <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return register(registryType, registryName, $ -> factory.get());
    }

    public final <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        var registryKey = register(registryType, registryName, elementFactory);
        return holderFactory.apply(registryKey);
    }

    public final <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return registerForHolder(registryType, registryName, $ -> elementFactory.get(), holderFactory);
    }

    public final <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> factory) {
        return registerForHolder(registryType, registryName, factory, ApexDeferredHolder::new);
    }

    public final <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return registerForHolder(registryType, registryName, $ -> factory.get());
    }
    // endregion

    // region: Item
    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerForHolder(Registries.ITEM, registryName, $ -> factory.apply(propertiesFactory.get().setId(registryKey(Registries.ITEM, registryName))), DeferredItem::new);
    }

    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerItem(registryName, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Item.Properties properties) {
        return registerItem(registryName, factory, () -> properties);
    }

    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory) {
        return registerItem(registryName, factory, Item.Properties::new);
    }

    public final DeferredItem<Item> registerSimpleItem(String registryName, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(registryName, Item::new, propertiesFactory);
    }

    public final DeferredItem<Item> registerSimpleItem(String registryName, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleItem(registryName, () -> propertiesMutator.apply(new Item.Properties()));
    }

    public final DeferredItem<Item> registerSimpleItem(String registryName, Item.Properties properties) {
        return registerSimpleItem(registryName, () -> properties);
    }

    public final DeferredItem<Item> registerSimpleItem(String registryName) {
        return registerSimpleItem(registryName, Item.Properties::new);
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(registryName, properties -> factory.apply(block.get(), properties), propertiesFactory);
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerBlockItem(registryName, block, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return registerBlockItem(registryName, block, factory, () -> properties);
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(registryName, block, factory, Item.Properties::new);
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return registerBlockItem(registryName, block, BlockItem::new, propertiesFactory);
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleBlockItem(registryName, block, () -> propertiesMutator.apply(new Item.Properties()));
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Item.Properties properties) {
        return registerSimpleBlockItem(registryName, block, () -> properties);
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block) {
        return registerSimpleBlockItem(registryName, block, Item.Properties::new);
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerBlockItem(block.getId().getPath(), block, factory, propertiesFactory);
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerBlockItem(block, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return registerBlockItem(block, factory, () -> properties);
    }

    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(block, factory, Item.Properties::new);
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return registerSimpleBlockItem(block.getId().getPath(), block, propertiesFactory);
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleBlockItem(block, () -> propertiesMutator.apply(new Item.Properties()));
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Item.Properties properties) {
        return registerSimpleBlockItem(block, () -> properties);
    }

    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block) {
        return registerSimpleBlockItem(block, Item.Properties::new);
    }
    // endregion

    // region: Block
    public final <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerForHolder(Registries.BLOCK, registryName, $ -> factory.apply(propertiesFactory.get().setId(registryKey(Registries.BLOCK, registryName))), DeferredBlock::new);
    }

    public final <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, BlockBehaviour.Properties properties) {
        return registerBlock(registryName, factory, () -> properties);
    }

    public final DeferredBlock<Block> registerSimpleBlock(String registryName, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerBlock(registryName, Block::new, propertiesFactory);
    }

    public final DeferredBlock<Block> registerSimpleBlock(String registryName, BlockBehaviour.Properties properties) {
        return registerSimpleBlock(registryName, () -> properties);
    }
    // endregion

    // region: BlockEntity
    @SafeVarargs
    public final <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(String registryName, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerForHolder(Registries.BLOCK_ENTITY_TYPE, registryName, () -> {
            var blocks = Stream.of(validBlocks).map(Supplier::get).collect(Collectors.<Block>toSet());
            return new BlockEntityType<>(factory, blocks);
        }, DeferredBlockEntity::new);
    }

    public final <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerBlockEntity(block.getId().getPath(), factory, ArrayUtils.add(validBlocks, block));
    }

    public final <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return registerBlockEntity(block.getId().getPath(), factory, block);
    }
    // endregion

    // region: Entity
    public final <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category, UnaryOperator<EntityType.Builder<TEntity>> propertiesMutator) {
        return registerForHolder(Registries.ENTITY_TYPE, registryName, $ -> propertiesMutator.apply(EntityType.Builder.of(factory, category)).build(registryKey(Registries.ENTITY_TYPE, registryName)), DeferredEntity::new);
    }

    public final <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return registerEntity(registryName, factory, category, UnaryOperator.identity());
    }

    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<? extends Mob>> entityType, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(entityType.getId().getPath() + "_spawn_egg", properties -> new SpawnEggItem(entityType.value(), properties), propertiesFactory);
    }

    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<? extends Mob>> entityType, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSpawnEggItem(entityType, () -> propertiesMutator.apply(new Item.Properties()));
    }

    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<? extends Mob>> entityType, Item.Properties properties) {
        return registerSpawnEggItem(entityType, () -> properties);
    }

    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<? extends Mob>> entityType) {
        return registerSpawnEggItem(entityType, Item.Properties::new);
    }
    // endregion

    // region: DataComponent
    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, UnaryOperator<DataComponentType.Builder<TData>> builder) {
        return registerForHolder(Registries.DATA_COMPONENT_TYPE, registryName, () -> builder.apply(DataComponentType.builder()).build(), DeferredDataComponent::new);
    }

    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, StreamCodec<RegistryFriendlyByteBuf, TData> streamCodec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
    }

    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, Codec<TData> networkCodec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(networkCodec)));
    }

    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec));
    }
    // endregion

    // region: CreativeModeTab
    public final ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, UnaryOperator<CreativeModeTab.Builder> builder) {
        return register(Registries.CREATIVE_MODE_TAB, registryName, () -> builder.apply(CreativeModeTab.builder().title(Component.translatable(creativeModeTabKey(registryName(registryName))))).build());
    }

    public final ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator itemsGenerator) {
        return registerCreativeModeTab(registryName, builder -> builder.icon(icon).displayItems(itemsGenerator));
    }
    // endregion

    // region: Attachment
    public final <TData> DeferredAttachment<TData> registerAttachment(String registryName, UnaryOperator<AttachmentType.Builder<TData>> builder, Function<IAttachmentHolder, TData> defaultValueFactory) {
        return registerForHolder(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, registryName, () -> builder.apply(AttachmentType.builder(defaultValueFactory)).build(), DeferredAttachment::new);
    }

    public final <TData> DeferredAttachment<TData> registerAttachment(String registryName, UnaryOperator<AttachmentType.Builder<TData>> builder, Supplier<TData> defaultValueFactory) {
        return registerAttachment(registryName, builder, holder -> defaultValueFactory.get());
    }
    // endregion

    // region: EntityDataSerializer
    public final <TData> DeferredEntityDataSerializer<TData> registerEntityDataSerializer(String registryName, StreamCodec<? super RegistryFriendlyByteBuf, TData> steamCodec, UnaryOperator<TData> copier) {
        return registerForHolder(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, registryName, () -> new EntityDataSerializer<TData>() {
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, TData> codec() {
                return steamCodec;
            }

            @Override
            public TData copy(TData value) {
                return copier.apply(value);
            }
        }, DeferredEntityDataSerializer::new);
    }

    public final <TData> DeferredEntityDataSerializer<TData> registerEntityDataSerializer(String registryName, StreamCodec<? super RegistryFriendlyByteBuf, TData> steamCodec) {
        return registerEntityDataSerializer(registryName, steamCodec, UnaryOperator.identity());
    }
    // endregion

    // region: Menu
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerForHolder(Registries.MENU, registryName, () -> new MenuType<>(factory, requiredFeatures), DeferredMenu::new);
    }

    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature));
    }

    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory) {
        return registerMenu(registryName, factory, FeatureFlags.VANILLA_SET);
    }

    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerMenu(registryName, (MenuType.MenuSupplier<TMenu>) factory, requiredFeatures);
    }

    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature));
    }

    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory) {
        return registerMenu(registryName, factory, FeatureFlags.VANILLA_SET);
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures) {
        var holder = registerMenu(registryName, factory);
        withEventBus(modBus -> modBus.addListener(EventPriority.LOW, RegisterMenuScreensEvent.class, event -> event.register(holder.value(), screenFactory.get())));
        return holder;
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature));
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlags.VANILLA_SET);
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures) {
        return registerMenu(registryName, (MenuType.MenuSupplier<TMenu>) factory, screenFactory, requiredFeatures);
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature));
    }

    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlags.VANILLA_SET);
    }
    // endregion

    // region: FluidType
    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, Supplier<FluidType.Properties> propertiesFactory) {
        return registerForHolder(NeoForgeRegistries.Keys.FLUID_TYPES, registryName, $ -> factory.apply(propertiesFactory.get()), DeferredFluidType::new);
    }

    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return registerFluidType(registryName, factory, () -> propertiesMutator.apply(FluidType.Properties.create()));
    }

    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, FluidType.Properties properties) {
        return registerFluidType(registryName, factory, () -> properties);
    }

    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory) {
        return registerFluidType(registryName, factory, FluidType.Properties::create);
    }

    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, Supplier<FluidType.Properties> propertiesFactory) {
        return registerFluidType(registryName, FluidType::new, propertiesFactory);
    }

    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return registerSimpleFluidType(registryName, () -> propertiesMutator.apply(FluidType.Properties.create()));
    }

    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, FluidType.Properties properties) {
        return registerSimpleFluidType(registryName, () -> properties);
    }

    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName) {
        return registerSimpleFluidType(registryName, FluidType.Properties::create);
    }
    // endregion

    // region: Fluid
    public final <TFluid extends Fluid> DeferredFluid<TFluid> registerFluid(String registryName, Supplier<TFluid> factory) {
        return registerForHolder(Registries.FLUID, registryName, factory, DeferredFluid::new);
    }
    // endregion

    // region: RecipeSerializer
    public final <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> registerRecipeSerializer(String registryName, MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return registerForHolder(Registries.RECIPE_SERIALIZER, registryName, () -> new SimpleRecipeSerializer<>(codec, streamCodec), DeferredRecipeSerializer::new);
    }
    // endregion
    // endregion

    // region: GameRules
    public final <TValue extends GameRules.Value<TValue>> GameRules.Key<TValue> registerGameRule(String registryName, GameRules.Category category, GameRules.Type<TValue> type) {
        return GameRules.register(namespace + ':' + registryName, category, type);
    }

    // region: Boolean
    public final GameRules.Key<GameRules.BooleanValue> registerBooleanGameRule(String registryName, GameRules.Category category, boolean defaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> changeListener) {
        return registerGameRule(registryName, category, GameRules.BooleanValue.create(defaultValue, changeListener));
    }

    public final GameRules.Key<GameRules.BooleanValue> registerBooleanGameRule(String registryName, GameRules.Category category, boolean defaultValue) {
        return registerBooleanGameRule(registryName, category, defaultValue, (server, value) -> { });
    }
    // endregion

    // region: Integer
    public final GameRules.Key<GameRules.IntegerValue> registerIntegerGameRule(String registryName, GameRules.Category category, int defaultValue, BiConsumer<MinecraftServer, GameRules.IntegerValue> changeListener) {
        return registerGameRule(registryName, category, GameRules.IntegerValue.create(defaultValue, changeListener));
    }

    public final GameRules.Key<GameRules.IntegerValue> registerIntegerGameRule(String registryName, GameRules.Category category, int defaultValue) {
        return registerIntegerGameRule(registryName, category, defaultValue, (server, value) -> { });
    }
    // endregion
    // endregion

    public static String creativeModeTabKey(ResourceKey<CreativeModeTab> registryKey) {
        return creativeModeTabKey(registryKey.location());
    }

    public static String creativeModeTabKey(ResourceLocation registryName) {
        return registryName.toLanguageKey("itemGroup");
    }
}
