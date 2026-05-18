package dev.apexstudios.apexcore.testing;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntIterators;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;

public record MultiBlockBounds(int width, int height, int depth) implements Comparable<MultiBlockBounds> {
    public MultiBlockBounds {
        if(width < 0 || height < 0 || depth < 0) {
            throw new IllegalArgumentException("MultiBlockBounds cannot be negative");
        }
    }

    public int indexOf(int x, int y, int z) {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            throw new IndexOutOfBoundsException("Coordinates (" + toFriendlyString(x, y, z) + ") out of bounds for size [" + toFriendlyString() + ']');
        }

        return x + (y * width) + (z * width * height);
    }

    public int indexOf(Vector3ic local) {
        return indexOf(local.x(), local.y(), local.z());
    }

    public Vector3ic get(int index) {
        Objects.checkIndex(index, size());

        var x = index % width;
        var remaining = index / width;
        var y = remaining % height;
        var z = remaining / height;

        return new Vector3i(x, y, z);
    }

    public int get(Direction.Axis axis) {
        return axis.choose(width, height, depth);
    }

    public int size() {
        return width * height * depth;
    }

    public String toFriendlyString() {
        return toFriendlyString(width, height, depth);
    }

    public int compareTo(int width, int height, int depth) {
        var cmp = Integer.compare(this.width, width);

        if(cmp == 0) {
            cmp = Integer.compare(this.height, height);
        }

        if(cmp == 0) {
            cmp = Integer.compare(this.depth, depth);
        }

        return cmp;
    }

    @Override
    public int compareTo(MultiBlockBounds other) {
        return compareTo(other.width, other.height, other.depth);
    }

    public static BlockPos toWorld(Vector3ic local, BlockPos origin) {
        return origin.offset(local.x(), local.y(), local.z());
    }

    public static Vector3ic toLocal(BlockPos world, BlockPos origin) {
        return new Vector3i(
                world.getX() - origin.getX(),
                world.getY() - origin.getY(),
                world.getZ() - origin.getZ()
        );
    }

    public static String toFriendlyString(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    public static Iterable<BlockPos> positions(BlockPos origin, MultiBlockBounds bounds) {
        return () -> Iterators.transform(IntIterators.fromTo(0, bounds.size()), index -> toWorld(bounds.get(index), origin));
    }

    public static boolean isValidPlacement(Level level, BlockPos placePos, MultiBlockBounds bounds, BlockPlaceContext context) {
        for(var otherPos : positions(placePos, bounds)) {
            var otherBlockState = level.getBlockState(otherPos);

            if(!otherBlockState.canBeReplaced(context)) {
                return false;
            }

            if(!otherBlockState.canSurvive(level, otherPos)) {
                return false;
            }
        }

        return true;
    }

    public static void placeAt(Level level, BlockPos placePos, BlockState blockState, @Nullable LivingEntity placer, MultiBlockBounds bounds) {
        var block = blockState.getBlock();

        for(var otherPos : positions(placePos, bounds)) {
            if(otherPos.equals(placePos)) {
                continue;
            }

            if(!level.getBlockState(otherPos).is(block)) {
                level.destroyBlock(otherPos, true, placer);
                level.setBlock(otherPos, blockState, Block.UPDATE_CLIENTS);
                MultiBlockMasterPos.set(level, otherPos, placePos);
            }
        }
    }

    public static void destroyAt(Level level, BlockPos breakPos, BlockState blockState, @Nullable LivingEntity breaker, MultiBlockBounds bounds) {
        var masterPos = MultiBlockMasterPos.get(level, breakPos);
        MultiBlockMasterPos.remove(level, breakPos);
        var block = blockState.getBlock();

        for(var otherPos : positions(masterPos, bounds)) {
            if(otherPos.equals(breakPos)) {
                continue;
            }

            var otherBlockState = level.getBlockState(otherPos);

            if(otherBlockState.is(block)) {
                MultiBlockMasterPos.remove(level, otherPos);
                level.removeBlock(otherPos, false);
                playerWillDestroy(level, otherPos, otherBlockState, breaker);
            }
        }
    }

    // copied from Block
    private static void playerWillDestroy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity breaker) {
        if (blockState.is(BlockTags.GUARDED_BY_PIGLINS) && breaker instanceof Player player && level instanceof ServerLevel serverLevel) {
            PiglinAi.angerNearbyPiglins(serverLevel, player, false);
        }

        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(breaker, blockState));
    }
}
