package dev.apexstudios.apexcore.common.network;

import dev.apexstudios.apexcore.common.ApexCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetModifierKeyPacket(boolean state) implements CustomPacketPayload {
    public static final Type<ServerboundSetModifierKeyPacket> TYPE = new Type<>(ApexCore.identifier("serverbound/set_modifier_key"));
    public static final StreamCodec<ByteBuf, ServerboundSetModifierKeyPacket> STREAM_CODEC = ByteBufCodecs.BOOL.map(ServerboundSetModifierKeyPacket::new, ServerboundSetModifierKeyPacket::state);

    @Override
    public Type<ServerboundSetModifierKeyPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork((Runnable) () -> context.player().setData(ApexCore.PLAYER_MODIFIER, state));
    }
}
