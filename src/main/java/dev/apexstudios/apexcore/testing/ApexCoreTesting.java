package dev.apexstudios.apexcore.testing;

import dev.apexstudios.apexcore.api.placement.BlockItemPlacementEvent;
import dev.apexstudios.apexcore.common.ApexCore;
import dev.apexstudios.registree.api.holder.DeferredAttachmentType;
import dev.apexstudios.registree.api.holder.DeferredBlock;
import dev.apexstudios.registree.api.holder.DeferredItem;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ApexCore.ID)
public final class ApexCoreTesting {
    public static final DeferredBlock<TestMultiBlock> MULTI_BLOCK = ApexCore.REGISTREE.registerBlock(
            "multi_block",
            TestMultiBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
    );

    public static final DeferredItem<BlockItem> MULTI_BLOCK_ITEM = ApexCore.REGISTREE.registerSimpleBlockItem(MULTI_BLOCK);

    public static final DeferredAttachmentType<Long2LongMap> MULTI_BLOCK_MASTER_POS = ApexCore.REGISTREE.registerAttachmentType(
            "multi_block_master_pos",
            () -> new Long2LongOpenHashMap(),
            builder -> builder
                    .serialize(new MultiBlockMasterPos.Serializer())
                    .sync(new MultiBlockMasterPos.SyncHandler())
    );

    public ApexCoreTesting(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, BlockItemPlacementEvent.CollectAdditionalBlockStates.class, event -> {
            var blockState = event.blockState();
            var context = event.placeContext();

            var placePos = context.getClickedPos();

            if(!(blockState.getBlock() instanceof TestMultiBlock multiBlock)) {
                return;
            }

            var bounds = multiBlock.bounds();

            for(var otherPos : MultiBlockBounds.positions(placePos, bounds)) {
                if(otherPos.equals(placePos)) {
                    continue;
                }

                event.with(otherPos, blockState);
            }
        });
    }
}
