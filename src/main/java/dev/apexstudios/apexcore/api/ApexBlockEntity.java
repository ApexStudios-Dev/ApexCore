package dev.apexstudios.apexcore.api;

import java.util.Collections;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.mixins.BlockEntityTypeAccessor;

public class ApexBlockEntity extends BlockEntity {
    public ApexBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    protected static void appendBlocks(BlockEntityTypeAddBlocksEvent event, BlockEntityType<?> blockEntityType, Block... blocks) {
        var validBlocks = new HashSet<>(blockEntityType.getValidBlocks());
        Collections.addAll(validBlocks, blocks);
        ((BlockEntityTypeAccessor) blockEntityType).neoforge$setValidBlocks(validBlocks);
    }
}
