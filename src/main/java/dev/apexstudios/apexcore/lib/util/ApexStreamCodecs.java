package dev.apexstudios.apexcore.lib.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import net.minecraft.core.IdMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public interface ApexStreamCodecs {
    StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
    StreamCodec<ByteBuf, FluidState> FLUID_STATE = ByteBufCodecs.idMapper(Fluid.FLUID_STATE_REGISTRY);

    static <TBuffer extends ByteBuf, TValue extends Enum<TValue> & StringRepresentable> StreamCodec<TBuffer, TValue> forNamedEnum(Supplier<TValue[]> valuesSupplier, UnaryOperator<String> nameMapper) {
        var nameLookup = StringRepresentable.createNameLookup(valuesSupplier.get(), nameMapper);
        return ApexStreamCodecs.<TBuffer>stringUtf8(FriendlyByteBuf.MAX_STRING_LENGTH).map(nameLookup, StringRepresentable::getSerializedName);
    }

    static <TBuffer extends ByteBuf, TValue extends Enum<TValue> & StringRepresentable> StreamCodec<TBuffer, TValue> forNamedEnum(Supplier<TValue[]> valuesSupplier) {
        return forNamedEnum(valuesSupplier, UnaryOperator.identity());
    }

    static <TValue extends Enum<TValue>> StreamCodec<ByteBuf, TValue> forEnum(Class<TValue> enumType) {
        return ByteBufCodecs.VAR_INT.map(ordinal -> enumType.getEnumConstants()[ordinal], Enum::ordinal);
    }

    // region: Generic wrappers
    static <TBuffer extends ByteBuf> StreamCodec<TBuffer, byte[]> byteArray(final int maxSize) {
        return StreamCodec.of((buffer, value) -> {
            if(value.length > maxSize)
                throw new EncoderException("ByteArray with size " + value.length + " is bigger than allowed " + maxSize);

            FriendlyByteBuf.writeByteArray(buffer, value);
        }, buffer -> FriendlyByteBuf.readByteArray(buffer, maxSize));
    }

    static <TBuffer extends ByteBuf> StreamCodec<TBuffer, String> stringUtf8(final int maxLength) {
        return StreamCodec.of(
                (buffer, value) -> Utf8String.write(buffer, value, maxLength),
                buffer -> Utf8String.read(buffer, maxLength)
        );
    }

    static <TBuffer extends ByteBuf> StreamCodec<TBuffer, Tag> tagCodec(final Supplier<NbtAccounter> accounter) {
        return StreamCodec.of((buffer, value) -> {
            if(value == EndTag.INSTANCE)
                throw new EncoderException("Expected non-null compound tag");

            FriendlyByteBuf.writeNbt(buffer, value);
        }, buffer -> {
            var tag = FriendlyByteBuf.readNbt(buffer, accounter.get());

            if(tag == null)
                throw new DecoderException("Expected non-null compound tag");

            return tag;
        });
    }

    static <TBuffer extends ByteBuf> StreamCodec<TBuffer, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> accounterSupplier) {
        return ApexStreamCodecs.<TBuffer>tagCodec(accounterSupplier).map(tag -> {
            if(tag instanceof CompoundTag)
                return (CompoundTag) tag;

            throw new DecoderException("Not a compound tag: " + tag);
        }, Function.identity());
    }

    static <TBuffer extends ByteBuf, TData> StreamCodec<TBuffer, TData> fromCodecTrusted(Codec<TData> codec) {
        return fromCodec(codec, NbtAccounter::unlimitedHeap);
    }

    static <TBuffer extends ByteBuf, TData> StreamCodec<TBuffer, TData> fromCodec(Codec<TData> codec) {
        return fromCodec(codec, () -> NbtAccounter.create(2097152L));
    }

    static <TBuffer extends ByteBuf, TData> StreamCodec<TBuffer, TData> fromCodec(Codec<TData> codec, Supplier<NbtAccounter> accounterSupplier) {
        return ApexStreamCodecs.<TBuffer>tagCodec(accounterSupplier).map(
                tag -> codec.parse(NbtOps.INSTANCE, tag).getOrThrow(value -> new DecoderException("Failed to decode: " + value + " " + tag)),
                data -> codec.encodeStart(NbtOps.INSTANCE, data).getOrThrow(value -> new EncoderException("Failed to encode: " + value + " " + data))
        );
    }

    static <TBuffer extends ByteBuf, TData> StreamCodec<TBuffer, TData> idMapper(final IntFunction<TData> idLookup, final ToIntFunction<TData> idGetter) {
        return StreamCodec.of(
                (buffer, value) -> VarInt.write(buffer, idGetter.applyAsInt(value)),
                buffer -> idLookup.apply(VarInt.read(buffer))
        );
    }

    static <TBuffer extends ByteBuf, TData> StreamCodec<TBuffer, TData> idMapper(IdMap<TData> idMap) {
        return idMapper(idMap::byIdOrThrow, idMap::getIdOrThrow);
    }
    // endregion
}
