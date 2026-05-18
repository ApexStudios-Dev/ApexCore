package dev.apexstudios.apexcore.testing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class TestMultiBlock extends Block {
    public TestMultiBlock(Properties properties) {
        super(properties);
    }

    public MultiBlockBounds bounds() {
        return new MultiBlockBounds(2, 2, 2);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var placePos = context.getClickedPos();
        var bounds = bounds();

        if(!MultiBlockBounds.isValidPlacement(level, placePos, bounds, context)) {
            return null;
        }

        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos placePos, BlockState blockState, @Nullable LivingEntity placer, ItemStack tool) {
        MultiBlockBounds.placeAt(level, placePos, blockState, placer, bounds());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos breakPos, BlockState blockState, Player player) {
        MultiBlockBounds.destroyAt(level, breakPos, blockState, player, bounds());
        return super.playerWillDestroy(level, breakPos, blockState, player);
    }
}
