package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.core.seat.SeatSetup;
import dev.apexstudios.apexcore.core.util.TooltipMutationHandler;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentHelper;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponentTypes;
import dev.apexstudios.apexcore.lib.placement.PlacementRenderEvent;
import dev.apexstudios.apexcore.lib.registree.Registree;
import dev.apexstudios.apexcore.lib.util.ApexPackSources;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(ApexCore.ID)
public final class ApexCore {
    public static final String ID = "apexcore";

    public static final Registree REGISTREE = new Registree(ID);
    // public static final DeferredBlock<MultiBlock> MULTI_BLOCK = REGISTREE.registerBlock("multi_block", MultiBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
    // public static final DeferredItem<BlockItem> MULTI_BLOCK_ITEM = REGISTREE.registerSimpleBlockItem(MULTI_BLOCK);

    public ApexCore(IEventBus modBus) {
        REGISTREE.registerEvents(modBus);

        BlockComponentTypes.register();
        BlockEntityComponentTypes.register();
        TooltipMutationHandler.register(modBus);
        SeatSetup.register(modBus);

        modBus.addListener(AddPackFindersEvent.class, event ->  event.addPackFinders(
                identifier("packs/visual_vanilla"),
                PackType.SERVER_DATA,
                Component.literal("Visual Vanilla"),
                ApexPackSources.BUILT_IN_NOT_AUTO,
                false,
                Pack.Position.TOP
        ));

        NeoForge.EVENT_BUS.addListener(PlacementRenderEvent.DefaultBlockState.class, event -> {
            var blockState = event.defaultBlockState();
            var newBlockState = new AtomicReference<>(blockState);
            var holder = BlockComponentHelper.asHolder(blockState);

            if(holder != null) {
                var context = event.placeContext();
                holder.runForComponent(BlockComponentTypes.FACING, component -> newBlockState.getAndUpdate(state -> component.setFor(context, state)));
                holder.runForComponent(BlockComponentTypes.ROTATION, component -> newBlockState.getAndUpdate(state -> component.setFor(context, state)));
                holder.runForComponent(BlockComponentTypes.FLUID_LOGGED, component -> newBlockState.getAndUpdate(state -> component.setFor(context, state)));
            }

            event.setDefaultBlockState(newBlockState.get());
        });
    }

    public static ResourceLocation identifier(String identifier) {
        return ResourceLocation.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + ResourceLocation.NAMESPACE_SEPARATOR + identifier;
    }
}
