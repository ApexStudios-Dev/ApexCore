package dev.apexstudios.apexcore.lib.component.block.types;

import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.block.BaseBlockComponent;
import dev.apexstudios.apexcore.lib.component.block.BlockComponent;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCauldronBlockComponent<TBuilder extends BaseCauldronBlockComponent.Builder<TBuilder>> extends BaseBlockComponent {
    private final CauldronInteraction.InteractionMap interactions;

    protected BaseCauldronBlockComponent(ComponentHolder<BlockComponent, Block> holder, TBuilder builder) {
        super(holder);

        interactions = ((Builder<TBuilder>) Objects.requireNonNull(builder)).interactions;
    }

    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return false;
    }

    protected void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos pos, Fluid fluid) {

    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        var interaction = interactions.map().get(stack.getItem());
        return interaction == null ? super.useItemOn(stack, blockState, level, pos, player, hand, result) : interaction.interact(blockState, level, pos, player, hand, stack);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState blockState, PathComputationType pathType) {
        return false;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        var stalactitePos = PointedDripstoneBlock.findStalactiteTipAboveCauldron(level, pos);

        if(stalactitePos != null) {
            var fluid = PointedDripstoneBlock.getCauldronFillFluidType(level, stalactitePos);

            if(fluid != Fluids.EMPTY && canReceiveStalactiteDrip(fluid))
                receiveStalactiteDrip(blockState, level, pos, fluid);
        }
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        if(CauldronFluidContent.getForBlock(blockState.getBlock()) == null)
            level.invalidateCapabilities(pos);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if(CauldronFluidContent.getForBlock(blockState.getBlock()) == null)
            level.invalidateCapabilities(pos);
    }

    protected static boolean isEntityInsideContent(BlockPos pos, Entity entity, double contentHeight) {
        return entity.getY() < (double) pos.getY() + contentHeight &&
                entity.getBoundingBox().maxY > pos.getY() + .25D;
    }

    public static class Builder<TSelf extends Builder<TSelf>> implements ComponentBuilder {
        @Nullable private CauldronInteraction.InteractionMap interactions = null;

        public TSelf interactions(CauldronInteraction.InteractionMap interactions) {
            this.interactions = interactions;
            return (TSelf) this;
        }
    }

    public static final class SimpleBuilder extends BaseCauldronBlockComponent.Builder<SimpleBuilder> {

    }
}
