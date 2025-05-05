package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.core.seat.SeatSetup;
import dev.apexstudios.apexcore.core.util.TooltipMutationHandler;
import dev.apexstudios.apexcore.lib.block.FacingBlock;
import dev.apexstudios.apexcore.lib.block.FluidLoggedBlock;
import dev.apexstudios.apexcore.lib.component.block.BlockComponentTypes;
import dev.apexstudios.apexcore.lib.placement.PlacementRenderEvent;
import dev.apexstudios.apexcore.lib.registree.Registree;
import dev.apexstudios.apexcore.lib.tooltip.RegisterTooltipEvent;
import dev.apexstudios.apexcore.lib.tooltip.TooltipPosition;
import dev.apexstudios.apexcore.lib.util.ApexPackSources;
import dev.apexstudios.apexcore.lib.util.ApexTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
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

        ApexTags.register();
        BlockComponentTypes.register();
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

        // modBus.addListener(RegisterTooltipEvent.class, ApexCore::tooltipTests);

        NeoForge.EVENT_BUS.addListener(PlacementRenderEvent.DefaultBlockState.class, event -> {
            var blockState = event.defaultBlockState();
            var block = blockState.getBlock();

            if(block instanceof FacingBlock facing)
                blockState = blockState.setValue(facing.facingProperty(), facing.facingForPlacement(event.placeContext()));
            if(block instanceof FluidLoggedBlock fluidLogged)
                blockState = blockState.setValue(fluidLogged.fluidLoggedProperty(), fluidLogged.isFluidLoggedForPlacement(event.placeContext()));

            event.setDefaultBlockState(blockState);
        });

        NeoForge.EVENT_BUS.addListener(PlacementRenderEvent.ModifyBlockState.class, event -> {
            var blockState = event.originalBlockState();

            if (blockState.is(Blocks.LECTERN)) {
                var stack = event.placeContext().getItemInHand();
                var customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);

                if (customData.contains("Book"))
                    event.withProperty(LecternBlock.HAS_BOOK, () -> true);
            }
        });

        // TODO
        /*NeoForge.EVENT_BUS.addListener(CanPlayerSleepEvent.class, event -> {
            var blockState = event.getState();
            var componentHolder = BlockComponentHelper.asHolder(blockState);

            if(componentHolder == null)
                return;

            var bedComponent = componentHolder.getComponent(BlockComponentTypes.BED);

            if(bedComponent == null)
                return;

            var vanillaProblem = event.getVanillaProblem();
            var moddedProblem = event.getProblem();
            var actualProblem = moddedProblem == null ? vanillaProblem : moddedProblem;

            if(moddedProblem != null && actualProblem != Player.BedSleepingProblem.TOO_FAR_AWAY && actualProblem != Player.BedSleepingProblem.OBSTRUCTED)
                return;

            event.setProblem(null);

            bedComponent.runForHead(event.getPos(), blockState, (headPos, headBlockState) -> {
                var entity = event.getEntity();
                var facingComponent = componentHolder.getComponent(BlockComponentTypes.FACING);
                var facing = facingComponent == null ? Direction.NORTH : facingComponent.get(headBlockState).getOpposite();

                if(!entity.bedInRange(headPos, facing))
                    event.setProblem(Player.BedSleepingProblem.TOO_FAR_AWAY);
                else if(entity.bedBlocked(headPos, facing))
                    event.setProblem(Player.BedSleepingProblem.OBSTRUCTED);
            });
        });*/
    }

    public static ResourceLocation identifier(String identifier) {
        return ResourceLocation.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + ResourceLocation.NAMESPACE_SEPARATOR + identifier;
    }

    private static void tooltipTests(RegisterTooltipEvent event) {
        for(var position : TooltipPosition.values()) {
            var name = position.name();
            event.registerBefore(position, (stack, context, adder, player, flag) -> adder.accept(Component.literal("Before: ").append(name)));
            event.registerAfter(position, (stack, context, adder, player, flag) -> adder.accept(Component.literal("After: ").append(name)));
        }
    }
}
