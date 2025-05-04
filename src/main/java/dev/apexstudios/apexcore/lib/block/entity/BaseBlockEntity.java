package dev.apexstudios.apexcore.lib.block.entity;

import com.google.errorprone.annotations.ForOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BaseBlockEntity extends BlockEntity {
    protected BaseBlockEntity(BlockEntityType<? extends BaseBlockEntity> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    // region: Overrides
    @ForOverride
    public BlockState playerWillDestroy(Level level, BlockState blockState, Player player) {
        return blockState;
    }

    @ForOverride
    public int getAnalogOutputSignal(BlockState blockState, Level level) {
        return -1;
    }
    // endregion

    // region: Internal
    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    // endregion
}
