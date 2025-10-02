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

public final class GetDefaultBlockPlacementStateEvent extends Event {
    private final LevelReader realLevel;
    private final BlockPlaceContext placeContext;
    private final BlockState defaultBlockState;
    @Nullable
    private BlockState newDefaultBlockState;

    private GetDefaultBlockPlacementStateEvent(LevelReader realLevel, BlockPlaceContext placeContext, BlockState defaultBlockState) {
        this.realLevel = realLevel;
        this.placeContext = placeContext;
        this.defaultBlockState = defaultBlockState;
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

    public BlockState defaultBlockState() {
        return newDefaultBlockState == null ? defaultBlockState : newDefaultBlockState;
    }

    public void setDefaultBlockState(BlockState defaultBlockState) {
        newDefaultBlockState = defaultBlockState;
    }

    public <TProperty extends Comparable<TProperty>> void withProperty(Property<TProperty> property, Supplier<TProperty> value) {
        var blockState = defaultBlockState();

        if(blockState.hasProperty(property))
            setDefaultBlockState(blockState.trySetValue(property, value.get()));
    }

    public static BlockState get(LevelReader realLevel, BlockPlaceContext context, BlockState blockState) {
        return NeoForge.EVENT_BUS.post(new GetDefaultBlockPlacementStateEvent(realLevel, context, blockState)).defaultBlockState();
    }
}
