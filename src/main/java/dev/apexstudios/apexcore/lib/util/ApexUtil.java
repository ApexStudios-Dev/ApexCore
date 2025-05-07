package dev.apexstudios.apexcore.lib.util;

import com.google.common.base.Predicates;
import dev.apexstudios.apexcore.mixin.LootTableAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.GameData;
import org.jetbrains.annotations.Nullable;

public interface ApexUtil {
    static boolean isInWorldBounds(LevelHeightAccessor level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && isInWorldBoundsHorizontal(pos);
    }

    static boolean isInWorldBoundsHorizontal(BlockPos pos) {
        return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000;
    }

    static <TLevel extends LevelHeightAccessor & CollisionGetter> boolean isInBounds(TLevel level, BlockPos pos) {
        if(!isInWorldBounds(level, pos))
            return false;
        if(!level.getWorldBorder().isWithinBounds(pos))
            return false;

        return true;
    }

    static boolean canPlace(LevelReader level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer) {
        if(!blockState.isAir() && !level.getBlockState(pos).canBeReplaced())
            return false;

        return mayPlace(level, pos, blockState, placer);
    }

    static boolean canPlace(BlockPlaceContext context, BlockState blockState) {
        var level = context.getLevel();
        var pos = context.getClickedPos();

        if(!blockState.isAir() && !level.getBlockState(pos).canBeReplaced(context))
            return false;

        return mayPlace(level, pos, blockState, context.getPlayer());
    }

    private static boolean mayPlace(LevelReader level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer) {
        if(!blockState.canSurvive(level, pos))
            return false;

        var collisionContext = placer == null ? CollisionContext.empty() : CollisionContext.of(placer);

        if(!level.isUnobstructed(blockState, pos, collisionContext))
            return false;

        if(placer instanceof Player player) {
            if(!player.mayBuild())
                return false;
            if(level instanceof Level lvl && !lvl.mayInteract(player, pos))
                return false;
        }

        return true;
    }

    static <TValue> void addAll(Collection<? super TValue> collection, Iterable<? extends TValue> values) {
        for(var value : values) {
            collection.add(value);
        }
    }

    @SafeVarargs
    static <TValue> void removeAll(Collection<? super TValue> collection, TValue... values) {
        for(var value : values) {
            collection.remove(value);
        }
    }

    static <TValue> void removeAll(Collection<? super TValue> collection, Iterable<? extends TValue> values) {
        for(var value : values) {
            collection.remove(value);
        }
    }

    static BlockState withPropertiesOf(BlockState source, BlockState target) {
        for(var property : target.getProperties()) {
            if(source.hasProperty(property))
                source = Block.copyProperty(source, target, property);
        }

        return source;
    }

    static <T> Class<T> asClass(Class<? super T> clazz) {
        return (Class<T>) clazz;
    }

    // copy of RandomizableContainer.unpackLootTable for common usages
    static void unpackLootTable(Level level, BlockPos pos, @Nullable Player player, @Nullable ResourceKey<LootTable> lootTableId, BiConsumer<LootTable, LootParams> filler) {
        if(lootTableId == null || !(level instanceof ServerLevel sLevel))
            return;

        var lootTable = sLevel.getServer().reloadableRegistries().getLootTable(lootTableId);
        var params = new LootParams.Builder(sLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));

        if(player != null)
            params = params.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);

        filler.accept(lootTable, params.create(LootContextParamSets.CHEST));
    }

    static void unpackLootTable(Level level, BlockPos pos, @Nullable Player player, @Nullable ResourceKey<LootTable> lootTableId, long seed, IItemHandlerModifiable itemHandler) {
        unpackLootTable(level, pos, player, lootTableId, (lootTable, params) -> fillItemHandlerFromLootTable(lootTable, itemHandler, params, seed));
    }

    // copy of LootTable.fill for IItemHandler types
    static void fillItemHandlerFromLootTable(LootTable lootTable, IItemHandlerModifiable itemHandler, LootParams params, long seed) {
        var accessor = (LootTableAccessor) lootTable;
        var context = new LootContext.Builder(params).withOptionalRandomSeed(seed).create(accessor.ApexCore$getRandomSequence());
        var items = accessor.ApexCore$getRandomItems(context);
        var random = context.getRandom();
        var slots = getAvailableSlots(itemHandler, random);
        accessor.ApexCore$shuffleAndSplitItems(items, slots.size(), random);

        for(var item : items) {
            if(slots.isEmpty()) {
                LootTableAccessor.ApexCore$getLogger().warn("Tried to over-fill a item handler");
                return;
            }

            itemHandler.setStackInSlot(slots.removeLast(), item.isEmpty() ? ItemStack.EMPTY : item);
        }
    }

    // copy of LootTable.getAvailableSlots for IItemHandler types
    private static List<Integer> getAvailableSlots(IItemHandler itemHandler, RandomSource random) {
        var slots = new ObjectArrayList<Integer>();

        for(var i = 0; i < itemHandler.getSlots(); i++) {
            if(itemHandler.getStackInSlot(i).isEmpty())
                slots.add(i);
        }

        Util.shuffle(slots, random);
        return slots;
    }

    static void registerPoiBlockState(ResourceKey<PoiType> poiType, BlockState blockState) {
        var holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(poiType);
        GameData.getBlockStatePointOfInterestTypeMap().put(blockState, holder);
    }

    static void registerPoiBlockStates(ResourceKey<PoiType> poiType, Block block, Predicate<BlockState> filter) {
        for(var blockState : block.getStateDefinition().getPossibleStates()) {
            if(filter.test(blockState))
                registerPoiBlockState(poiType, blockState);
        }
    }

    static void registerPoiBlockStates(ResourceKey<PoiType> poiType, Block block) {
        registerPoiBlockStates(poiType, block, Predicates.alwaysTrue());
    }
}
