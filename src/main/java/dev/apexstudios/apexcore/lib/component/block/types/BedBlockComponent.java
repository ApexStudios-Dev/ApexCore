package dev.apexstudios.apexcore.lib.component.block.types;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import dev.apexstudios.apexcore.lib.util.ApexUtil;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// Requires MultiBlock component
public final class BedBlockComponent extends BaseBlockComponent {
    public static final ComponentType<BlockComponent, BedBlockComponent, Block, Builder> COMPONENT_TYPE = ComponentType.registerBlock(
            ApexCore.identifier("bed"),
            Builder::new,
            BedBlockComponent::new
    );

    public static final BooleanProperty OCCUPIED = BedBlock.OCCUPIED;

    private final BiMap<Integer, Integer> indices;

    private BedBlockComponent(ComponentHolder<BlockComponent, Block> holder, Builder builder) {
        super(holder);

        if(builder.indices.isEmpty())
            throw new IllegalStateException("No Head<->Tail bed index mapping provided");

        indices = ImmutableBiMap.copyOf(builder.indices);
    }

    public boolean isHead(BlockState blockState) {
        var index = getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK).indexOf(blockState);
        return indices.containsKey(index);
    }

    public boolean isOccupied(BlockState blockState) {
        return blockState.getValue(OCCUPIED);
    }

    public void runForOpposite(BlockPos pos, BlockState blockState, BiConsumer<BlockPos, BlockState> consumer) {
        var multiBlock = getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK);
        var index = multiBlock.indexOf(blockState);
        var otherPos = pos;
        var otherBlockState = blockState;

        if(indices.containsKey(index)) {
            otherBlockState = multiBlock.withIndex(blockState, indices.get(index));
            var origin = multiBlock.getOrigin(pos, blockState);
            otherPos = multiBlock.getPos(origin, otherBlockState);
        }

        consumer.accept(otherPos, otherBlockState);
    }

    public void setOccupied(Level level, BlockPos pos, BlockState blockState, boolean occupied) {
        level.setBlock(pos, blockState.setValue(OCCUPIED, occupied), Block.UPDATE_ALL);
        runForOpposite(pos, blockState, (oppositePos, oppositeBlockState) -> level.setBlock(oppositePos, oppositeBlockState.setValue(OCCUPIED, occupied), Block.UPDATE_ALL));
    }

    @Override
    public BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState.setValue(OCCUPIED, false);
    }

    @Override
    public void createBlockStateDefinition(Consumer<Property<?>> consumer) {
        consumer.accept(OCCUPIED);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if(!level.isClientSide) {
            var interactPos = pos;
            var interactBlockState = blockState;

            if(!isHead(blockState)) {
                var multiBlock = getComponentOrThrow(BlockComponentTypes.MULTI_BLOCK);
                var origin = multiBlock.getOrigin(pos, blockState);
                interactBlockState = multiBlock.withIndex(blockState, indices.inverse().get(multiBlock.indexOf(blockState)));
                interactPos = multiBlock.getPos(origin, interactBlockState);
            }

            if(!BedBlock.canSetSpawn(level)) {
                level.removeBlock(interactPos, false);

                var center = interactPos.getCenter();
                level.explode(null, level.damageSources().badRespawnPointExplosion(center), null, center, 5F, true, Level.ExplosionInteraction.BLOCK);
            } else if(isOccupied(interactBlockState)) {
                if(!kickVillagerOutOfBed(level, interactPos))
                    player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            } else {
                player.startSleepInBed(interactPos).ifLeft(problem -> {
                    var msg = problem.getMessage();

                    if(msg != null)
                        player.displayClientMessage(msg, true);
                });
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState blockState, EntityType<?> entityType, LevelReader level, BlockPos pos, float orientation) {
        var facing = findComponent(BlockComponentTypes.FACING).map(component -> component.get(blockState)).orElse(Direction.NORTH);
        return BedBlock.findStandUpPosition(entityType, level, pos, facing, orientation).map(vec -> ServerPlayer.RespawnPosAngle.of(vec, pos));
    }

    private boolean kickVillagerOutOfBed(Level level, BlockPos pos) {
        var villagers = level.getEntitiesOfClass(Villager.class, new AABB(pos), LivingEntity::isSleeping);

        if(villagers.isEmpty())
            return false;

        villagers.getFirst().stopSleeping();
        return true;
    }

    public static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> void registerPoi(IEventBus modBus, Supplier<TBlock> blockSupplier) {
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(() -> registerPoi(blockSupplier.get())));
    }

    public static <TBlock extends Block & ComponentHolder<BlockComponent, Block>> void registerPoi(TBlock block) {
        block.runForComponent(COMPONENT_TYPE, component -> ApexUtil.registerPoiBlockStates(PoiTypes.HOME, block, component::isHead));
    }

    public static final class Builder implements ComponentBuilder {
        private final BiMap<Integer, Integer> indices = HashBiMap.create();

        public Builder indices(int head, int foot) {
            indices.put(head, foot);
            return this;
        }
    }
}
