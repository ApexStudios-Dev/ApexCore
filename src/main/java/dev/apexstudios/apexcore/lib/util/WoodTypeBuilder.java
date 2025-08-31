package dev.apexstudios.apexcore.lib.util;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class WoodTypeBuilder {
    private final BlockSetTypeBuilder blockSetType = BlockSetTypeBuilder.builder();
    private Supplier<SoundType> soundType = () -> SoundType.WOOD;
    private Supplier<SoundType> hangingSignSoundType = () -> SoundType.HANGING_SIGN;
    private Supplier<SoundEvent> fenceGateClose = () -> SoundEvents.FENCE_GATE_CLOSE;
    private Supplier<SoundEvent> fenceGateOpen = () -> SoundEvents.FENCE_GATE_OPEN;

    private WoodTypeBuilder() {
    }

    public WoodTypeBuilder soundType(Supplier<SoundType> soundType) {
        this.soundType = soundType;
        return this;
    }

    public WoodTypeBuilder hangingSignSoundType(Supplier<SoundType> hangingSignSoundType) {
        this.hangingSignSoundType = hangingSignSoundType;
        return this;
    }

    public WoodTypeBuilder fenceGateClose(Supplier<SoundEvent> fenceGateClose) {
        this.fenceGateClose = fenceGateClose;
        return this;
    }

    public WoodTypeBuilder fenceGateOpen(Supplier<SoundEvent> fenceGateOpen) {
        this.fenceGateOpen = fenceGateOpen;
        return this;
    }

    public WoodTypeBuilder blockSetType(Consumer<BlockSetTypeBuilder> blockSetTypeBuilder) {
        blockSetTypeBuilder.accept(blockSetType);
        return this;
    }

    public WoodTypeBuilder copy(WoodType woodType) {
        return soundType(woodType::soundType)
                .hangingSignSoundType(woodType::hangingSignSoundType)
                .fenceGateClose(woodType::fenceGateClose)
                .fenceGateOpen(woodType::fenceGateOpen)
                .blockSetType(builder -> builder.copy(woodType.setType()));
    }

    public WoodType build(String woodTypeName, String blockSetTypeName) {
        return new WoodType(
                woodTypeName,
                blockSetType.build(blockSetTypeName),
                soundType.get(),
                hangingSignSoundType.get(),
                fenceGateClose.get(),
                fenceGateOpen.get()
        );
    }

    public WoodType build(String name) {
        return build(name, name);
    }

    public static WoodTypeBuilder builder() {
        return new WoodTypeBuilder();
    }
}
