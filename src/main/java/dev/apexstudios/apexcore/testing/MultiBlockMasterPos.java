package dev.apexstudios.apexcore.testing;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public interface MultiBlockMasterPos {
    static void set(Level level, BlockPos pos, BlockPos masterPos) {
        if(pos.equals(masterPos)) {
            return;
        }

        var map = level.getData(ApexCoreTesting.MULTI_BLOCK_MASTER_POS);
        map.put(pos.asLong(), masterPos.asLong());
        level.syncData(ApexCoreTesting.MULTI_BLOCK_MASTER_POS);
    }

    static BlockPos get(Level level, BlockPos pos) {
        var map = level.getExistingDataOrNull(ApexCoreTesting.MULTI_BLOCK_MASTER_POS);

        if(map == null) {
            return pos;
        }

        var key = pos.asLong();

        if(map.containsKey(key)) {
            return BlockPos.of(map.get(key));
        }

        return pos;
    }

    static void remove(Level level, BlockPos pos) {
        var map = level.getExistingDataOrNull(ApexCoreTesting.MULTI_BLOCK_MASTER_POS);

        if(map == null) {
            return;
        }

        map.remove(pos.asLong());

        if(map.isEmpty()) {
            level.removeData(ApexCoreTesting.MULTI_BLOCK_MASTER_POS);
        }

        level.syncData(ApexCoreTesting.MULTI_BLOCK_MASTER_POS);
    }

    @ApiStatus.Internal
    final class Serializer implements IAttachmentSerializer<Long2LongMap> {
        @Override
        public Long2LongMap read(IAttachmentHolder holder, ValueInput input) {
            var map = new Long2LongOpenHashMap();

            for(var pos : input.keySet()) {
                input.getLong(pos).ifPresent(masterPos -> map.put(
                        Long.parseLong(pos),
                        masterPos.longValue()
                ));
            }

            return map;
        }

        @Override
        public boolean write(Long2LongMap map, ValueOutput output) {
            if(map.isEmpty()) {
                return false;
            }

            map.forEach((pos, masterPos) -> output.putLong(Long.toString(pos), masterPos));
            return true;
        }
    }

    @ApiStatus.Internal
    final class SyncHandler implements AttachmentSyncHandler<Long2LongMap> {
        @Override
        public void write(RegistryFriendlyByteBuf buf, Long2LongMap map, boolean initialSync) {
            var size = map.size();
            ByteBufCodecs.VAR_INT.encode(buf, size);

            if(size == 0) {
                return;
            }

            map.forEach((pos, masterPos) -> {
                ByteBufCodecs.VAR_LONG.encode(buf, pos);
                ByteBufCodecs.VAR_LONG.encode(buf, masterPos);
            });
        }

        @Override
        public @Nullable Long2LongMap read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable Long2LongMap previousValue) {
            var size = ByteBufCodecs.VAR_INT.decode(buf);

            if(size == 0) {
                return null;
            }

            if(previousValue != null) {
                previousValue.clear();
            }

            var map = previousValue == null ? new Long2LongOpenHashMap() : previousValue;

            for(var i = 0; i < size; i++) {
                map.put(ByteBufCodecs.VAR_LONG.decode(buf), ByteBufCodecs.VAR_LONG.decode(buf));
            }

            return map;
        }
    }
}
