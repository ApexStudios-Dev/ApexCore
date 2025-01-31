package dev.apexstudios.apexcore.lib.util;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public final class BlockSetTypeBuilder {
    // defaults match should OAK
    private boolean canOpenByHand = true;
    private boolean canOpenByWindCharge = true;
    private boolean canButtonBeActivatedByArrows = true;
    private BlockSetType.PressurePlateSensitivity pressurePlateSensitivity = BlockSetType.PressurePlateSensitivity.EVERYTHING;
    private SoundType soundType = SoundType.WOOD;
    private Supplier<SoundEvent> doorClose = () -> SoundEvents.WOODEN_DOOR_CLOSE;
    private Supplier<SoundEvent> doorOpen = () -> SoundEvents.WOODEN_DOOR_OPEN;
    private Supplier<SoundEvent> trapDoorClose = () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE;
    private Supplier<SoundEvent> trapDoorOpen = () -> SoundEvents.WOODEN_TRAPDOOR_OPEN;
    private Supplier<SoundEvent> pressurePlateClickOff = () -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF;
    private Supplier<SoundEvent> pressurePlateClickOn = () -> SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON;
    private Supplier<SoundEvent> buttonClickOff = () -> SoundEvents.WOODEN_BUTTON_CLICK_OFF;
    private Supplier<SoundEvent> buttonClickOn = () -> SoundEvents.WOODEN_BUTTON_CLICK_ON;

    private BlockSetTypeBuilder() {
    }

    public BlockSetTypeBuilder canOpenByHand(boolean canOpenByHand) {
        this.canOpenByHand = canOpenByHand;
        return this;
    }

    public BlockSetTypeBuilder canOpenByWindCharge(boolean canOpenByWindCharge) {
        this.canOpenByWindCharge = canOpenByWindCharge;
        return this;
    }

    public BlockSetTypeBuilder canButtonBeActivatedByArrows(boolean canButtonBeActivatedByArrows) {
        this.canButtonBeActivatedByArrows = canButtonBeActivatedByArrows;
        return this;
    }

    public BlockSetTypeBuilder soundType(SoundType soundType) {
        this.soundType = soundType;
        return this;
    }

    public BlockSetTypeBuilder doorClose(Supplier<SoundEvent> doorClose) {
        this.doorClose = doorClose;
        return this;
    }

    public BlockSetTypeBuilder doorOpen(Supplier<SoundEvent> doorOpen) {
        this.doorOpen = doorOpen;
        return this;
    }

    public BlockSetTypeBuilder pressurePlateSensitivity(BlockSetType.PressurePlateSensitivity pressurePlateSensitivity) {
        this.pressurePlateSensitivity = pressurePlateSensitivity;
        return this;
    }

    public BlockSetTypeBuilder trapDoorClose(Supplier<SoundEvent> trapDoorClose) {
        this.trapDoorClose = trapDoorClose;
        return this;
    }

    public BlockSetTypeBuilder trapDoorOpen(Supplier<SoundEvent> trapDoorOpen) {
        this.trapDoorOpen = trapDoorOpen;
        return this;
    }

    public BlockSetTypeBuilder pressurePlateClickOff(Supplier<SoundEvent> pressurePlateClickOff) {
        this.pressurePlateClickOff = pressurePlateClickOff;
        return this;
    }

    public BlockSetTypeBuilder pressurePlateClickOn(Supplier<SoundEvent> pressurePlateClickOn) {
        this.pressurePlateClickOn = pressurePlateClickOn;
        return this;
    }

    public BlockSetTypeBuilder buttonClickOff(Supplier<SoundEvent> buttonClickOff) {
        this.buttonClickOff = buttonClickOff;
        return this;
    }

    public BlockSetTypeBuilder buttonClickOn(Supplier<SoundEvent> buttonClickOn) {
        this.buttonClickOn = buttonClickOn;
        return this;
    }

    public BlockSetTypeBuilder copy(BlockSetType blockSetType) {
        return canOpenByHand(blockSetType.canOpenByHand())
                .canOpenByWindCharge(blockSetType.canOpenByWindCharge())
                .canButtonBeActivatedByArrows(blockSetType.canButtonBeActivatedByArrows())
                .soundType(blockSetType.soundType())
                .doorClose(blockSetType::doorClose)
                .doorOpen(blockSetType::doorOpen)
                .pressurePlateSensitivity(blockSetType.pressurePlateSensitivity())
                .trapDoorClose(blockSetType::trapdoorClose)
                .trapDoorOpen(blockSetType::trapdoorOpen)
                .pressurePlateClickOff(blockSetType::pressurePlateClickOff)
                .pressurePlateClickOn(blockSetType::pressurePlateClickOn)
                .buttonClickOff(blockSetType::buttonClickOff)
                .buttonClickOn(blockSetType::buttonClickOn);
    }

    public BlockSetType build(String name) {
        return BlockSetType.register(new BlockSetType(
                name,
                canOpenByHand,
                canOpenByWindCharge,
                canButtonBeActivatedByArrows,
                pressurePlateSensitivity,
                soundType,
                doorClose.get(),
                doorOpen.get(),
                trapDoorClose.get(),
                trapDoorOpen.get(),
                pressurePlateClickOff.get(),
                pressurePlateClickOn.get(),
                buttonClickOff.get(),
                buttonClickOn.get()
        ));
    }

    public static BlockSetTypeBuilder builder() {
        return new BlockSetTypeBuilder();
    }
}
