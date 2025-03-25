package dev.apexstudios.apexcore.lib.component.block.entity.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.BaseBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponentTypes;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import java.util.Objects;
import java.util.function.LongSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public final class LootTableBlockEntityComponent extends BaseBlockEntityComponent {
    public static final ComponentType<BlockEntityComponent, LootTableBlockEntityComponent, BlockEntity, ComponentBuilder> COMPONENT_TYPE = ComponentType.registerBlockEntity(
            ApexCore.identifier("loot_table"),
            LootTableBlockEntityComponent::new
    );

    public static final String NBT_LOOT_TABLE = RandomizableContainer.LOOT_TABLE_TAG;
    public static final String NBT_SEED = RandomizableContainer.LOOT_TABLE_SEED_TAG;

    @Nullable private ResourceKey<LootTable> lootTableId = null;
    private long seed = -1L;

    private LootTableBlockEntityComponent(ComponentHolder<BlockEntityComponent, BlockEntity> holder) {
        super(holder);
    }

    public @Nullable ResourceKey<LootTable> getLootTableId() {
        return lootTableId;
    }

    public long getSeed() {
        return seed;
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTableId, long seed) {
        setLootTable(lootTableId);
        setLootTableSeed(seed);
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTableId) {
        if(!Objects.equals(this.lootTableId, lootTableId)) {
            this.lootTableId = lootTableId;
            unwrap().setChanged();
        }
    }

    public void setLootTableSeed(long seed) {
        if(this.seed != seed) {
            this.seed = seed;
            unwrap().setChanged();
        }
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        lootTableId = tag.read(NBT_LOOT_TABLE, LootTable.KEY_CODEC).orElse(null);
        seed = tag.getLongOr(NBT_SEED, 0L);
    }

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if(lootTableId != null) {
            tag.putString(NBT_LOOT_TABLE, lootTableId.location().toString());

            if(seed != -1L)
                tag.putLong(NBT_SEED, seed);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        if (level.isClientSide || !player.isCreative())
            unpack(unwrap(), player);

        return super.playerWillDestroy(level, pos, blockState, player);
    }

    public static void unpack(BlockGetter level, BlockPos pos, @Nullable Player player) {
        var blockEntity = level.getBlockEntity(pos);

        if(blockEntity != null)
            unpack(blockEntity, player);
    }

    public static void unpack(BlockEntity blockEntity, @Nullable Player player) {
        if(!blockEntity.hasLevel() || !(blockEntity instanceof ComponentHolder<?, ?>))
            return;

        var holder = (ComponentHolder<BlockEntityComponent, BlockEntity>) blockEntity;
        var inventory = holder.getComponent(BlockEntityComponentTypes.INVENTORY);
        var lootTable = holder.getComponent(COMPONENT_TYPE);

        if(inventory == null || lootTable == null)
            return;

        // vanilla RandomizableContainer clears the stored loot table data
        // before generating loot and storing the items in the given inventory
        // this is to mimic that behavior
        //
        // this data must be cleared
        // otherwise loot will constantly be generated
        // each time the inventory is accessed
        var lootTableId = lootTable.getLootTableId();
        var seed = lootTable.getSeed();
        lootTable.setLootTable(null, -1L);

        ApexUtil.unpackLootTable(blockEntity.getLevel(), blockEntity.getBlockPos(), player, lootTableId, seed, inventory.getItemHandler());
    }

    public static void setLootTable(BlockGetter level, BlockPos pos, @Nullable ResourceKey<LootTable> lootTableId, LongSupplier seed) {
        var blockEntity = level.getBlockEntity(pos);

        if(blockEntity != null)
            setLootTable(blockEntity, lootTableId, seed);
    }

    public static void setLootTable(BlockGetter level, BlockPos pos, @Nullable ResourceKey<LootTable> lootTableId, RandomSource random) {
        var blockEntity = level.getBlockEntity(pos);

        if(blockEntity != null)
            setLootTable(blockEntity, lootTableId, random);
    }

    public static void setLootTable(BlockGetter level, BlockPos pos, @Nullable ResourceKey<LootTable> lootTableId, long seed) {
        var blockEntity = level.getBlockEntity(pos);

        if(blockEntity != null)
            setLootTable(blockEntity, lootTableId, seed);
    }

    public static void setLootTable(BlockGetter level, BlockPos pos, @Nullable ResourceKey<LootTable> lootTableId) {
        var blockEntity = level.getBlockEntity(pos);

        if(blockEntity != null)
            setLootTable(blockEntity, lootTableId);
    }

    public static void setLootTable(BlockEntity blockEntity, @Nullable ResourceKey<LootTable> lootTableId, LongSupplier seed) {
        if(blockEntity instanceof ComponentHolder)
            ((ComponentHolder<BlockEntityComponent, BlockEntity>) blockEntity).runForComponent(COMPONENT_TYPE, component -> component.setLootTable(lootTableId, seed.getAsLong()));
    }

    public static void setLootTable(BlockEntity blockEntity, @Nullable ResourceKey<LootTable> lootTableId, RandomSource random) {
        setLootTable(blockEntity, lootTableId, random::nextLong);
    }

    public static void setLootTable(BlockEntity blockEntity, @Nullable ResourceKey<LootTable> lootTableId, long seed) {
        setLootTable(blockEntity, lootTableId, () -> seed);
    }

    public static void setLootTable(BlockEntity blockEntity, @Nullable ResourceKey<LootTable> lootTableId) {
        if(blockEntity instanceof ComponentHolder)
            ((ComponentHolder<BlockEntityComponent, BlockEntity>) blockEntity).runForComponent(COMPONENT_TYPE, component -> component.setLootTable(lootTableId));
    }

    public static void setLootTableSeed(BlockEntity blockEntity, LongSupplier seed) {
        if(blockEntity instanceof ComponentHolder)
            ((ComponentHolder<BlockEntityComponent, BlockEntity>) blockEntity).runForComponent(COMPONENT_TYPE, component -> component.setLootTableSeed(seed.getAsLong()));
    }

    public static void setLootTableSeed(BlockEntity blockEntity, RandomSource random) {
        setLootTableSeed(blockEntity, random::nextLong);
    }

    public static void setLootTableSeed(BlockEntity blockEntity, long seed) {
        setLootTableSeed(blockEntity, () -> seed);
    }
}
