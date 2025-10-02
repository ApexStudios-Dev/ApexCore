package dev.apexstudios.apexcore.core;

import dev.apexstudios.apexcore.core.seat.SeatSetup;
import dev.apexstudios.apexcore.lib.placement.GetDefaultBlockPlacementStateEvent;
import dev.apexstudios.apexcore.lib.placement.SetBlockPlacementStateEvent;
import dev.apexstudios.apexcore.lib.registree.Registree;
import dev.apexstudios.apexcore.lib.util.ApexPackSources;
import dev.apexstudios.apexcore.lib.util.ApexTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(ApexCore.ID)
public final class ApexCore {
    public static final String ID = "apexcore";
    public static final Registree REGISTREE = new Registree(ID);

    public ApexCore(IEventBus modBus) {
        REGISTREE.registerEvents(modBus);

        ApexTags.register();
        SeatSetup.register(modBus);

        modBus.addListener(AddPackFindersEvent.class, event ->  event.addPackFinders(
                identifier("packs/visual_vanilla"),
                PackType.SERVER_DATA,
                Component.literal("Visual Vanilla"),
                ApexPackSources.BUILT_IN_NOT_AUTO,
                false,
                Pack.Position.TOP
        ));

        NeoForge.EVENT_BUS.addListener(GetDefaultBlockPlacementStateEvent.class, event -> {
            var pos = event.pos();
            var level = event.level();
            var placeContext = event.placeContext();

            event.withProperty(BlockStateProperties.WATERLOGGED, () -> level.getFluidState(pos).getType().isSame(Fluids.WATER));
            event.withProperty(BlockStateProperties.ROTATION_16, () -> RotationSegment.convertToSegment(placeContext.getRotation() + 180F));

            event.withProperty(BlockStateProperties.HORIZONTAL_FACING, () -> {
                var facing = placeContext.getHorizontalDirection();
                var blockState = event.defaultBlockState();

                if(blockState.getBlock() instanceof AbstractFurnaceBlock)
                    return facing.getOpposite();
                else if(blockState.is(BlockTags.ANVIL))
                    return facing.getClockWise();

                return facing;
            });

            event.withProperty(BlockStateProperties.FACING, () -> {
                if(event.defaultBlockState().is(Tags.Blocks.BARRELS))
                    return placeContext.getNearestLookingDirection().getOpposite();

                return placeContext.getHorizontalDirection();
            });
        });

        NeoForge.EVENT_BUS.addListener(SetBlockPlacementStateEvent.class, event -> {
            var blockState = event.originalBlockState();

            if (blockState.is(Blocks.LECTERN)) {
                var stack = event.placeContext().getItemInHand();
                var customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);

                if (customData != null && customData.contains("Book"))
                    event.withProperty(LecternBlock.HAS_BOOK, () -> true);
            }
        });
    }

    public static ResourceLocation identifier(String identifier) {
        return ResourceLocation.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + ResourceLocation.NAMESPACE_SEPARATOR + identifier;
    }
}
