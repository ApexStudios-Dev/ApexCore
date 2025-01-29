package dev.apexstudios.apexcore.extension;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21.5")
@Deprecated(forRemoval = true, since = "1.21.4")
public interface ApexCompoundTag {
    Logger LOGGER = LogUtils.getLogger();

    default <T> void store(String key, Codec<T> codec, T value) {
        store(key, codec, NbtOps.INSTANCE, value);
    }

    default <T> void store(String key, Codec<T> codec, DynamicOps<Tag> ops, T value) {
        self().put(key, codec.encodeStart(ops, value).getOrThrow());
    }

    default <T> void store(MapCodec<T> codec, T value) {
        store(codec, NbtOps.INSTANCE, value);
    }

    default <T> void store(MapCodec<T> codec, DynamicOps<Tag> ops, T value) {
        self().merge((CompoundTag) codec.encoder().encodeStart(ops, value).getOrThrow());
    }

    default <T> Optional<T> read(String key, Codec<T> codec) {
        return read(key, codec, NbtOps.INSTANCE);
    }

    default <T> Optional<T> read(String key, Codec<T> codec, DynamicOps<Tag> ops) {
        var tag = self().get(key);
        return tag == null ? Optional.empty() : codec.parse(ops, tag).resultOrPartial(err -> LOGGER.error("Failed to read field ({}={}): {}", key, tag, err));
    }

    default <T> Optional<T> read(MapCodec<T> codec) {
        return read(codec, NbtOps.INSTANCE);
    }

    default <T> Optional<T> read(MapCodec<T> codec, DynamicOps<Tag> ops) {
        return codec.decode(ops, ops.getMap(self()).getOrThrow()).resultOrPartial(err -> LOGGER.error("Failed to read value ({}): {}", self(), err));
    }

    private CompoundTag self() {
        return (CompoundTag) this;
    }
}
