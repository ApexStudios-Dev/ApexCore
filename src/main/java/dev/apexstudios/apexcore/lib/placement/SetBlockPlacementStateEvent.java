package dev.apexstudios.apexcore.lib.placement;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public final class SetBlockPlacementStateEvent extends Event {
    private final LevelReader realLevel;
    private final BlockPlaceContext placeContext;
    private final BlockState originalBlockState;
    @Nullable
    private BlockState newBlockState;

    private SetBlockPlacementStateEvent(LevelReader realLevel, BlockPlaceContext placeContext, BlockState originalBlockState) {
        this.realLevel = realLevel;
        this.placeContext = placeContext;
        this.originalBlockState = originalBlockState;
    }

    public LevelReader level() {
        return realLevel;
    }

    public BlockPos pos() {
        return placeContext.getClickedPos();
    }

    public BlockPlaceContext placeContext() {
        return placeContext;
    }

    public BlockState originalBlockState() {
        return originalBlockState;
    }

    public BlockState blockState() {
        return newBlockState == null ? originalBlockState : newBlockState;
    }

    public void setBlockState(BlockState blockState) {
        newBlockState = blockState;
    }

    public <TProperty extends Comparable<TProperty>> void withProperty(Property<TProperty> property, Supplier<TProperty> value) {
        var blockState = blockState();

        if(blockState.hasProperty(property))
            setBlockState(blockState.trySetValue(property, value.get()));
    }

    public static BlockState set(LevelReader realLevel, BlockPlaceContext placeContext, BlockState originalBlockState) {
        return NeoForge.EVENT_BUS.post(new SetBlockPlacementStateEvent(realLevel, placeContext, originalBlockState)).blockState();
    }
}
