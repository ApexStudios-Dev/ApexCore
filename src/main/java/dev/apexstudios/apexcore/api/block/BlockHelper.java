package dev.apexstudios.apexcore.api.block;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface BlockHelper {
    static boolean shouldBounceOnBlock(BlockPos pos, BlockState blockState, Entity entity) {
        if(entity.isSuppressingBounce()) {
            return false;
        }

        if(blockState.is(BlockTags.SUPPRESSES_BOUNCE)) {
            return false;
        }

        return entity.getBlockBounciness(pos, blockState) > 0D;
    }

    static <TBlockEntity extends BlockEntity> @Nullable TBlockEntity getBlockEntity(BlockGetter level, BlockPos pos, BlockEntityType<TBlockEntity> blockEntityType) {
        return blockEntityType.getBlockEntity(level, pos);
    }

    static <TBlockEntity extends BlockEntity> @Nullable TBlockEntity getBlockEntity(BlockGetter level, BlockPos pos, Supplier<BlockEntityType<TBlockEntity>> blockEntityType) {
        return getBlockEntity(level, pos, blockEntityType.get());
    }

    static <TBlockEntity extends BlockEntity> TBlockEntity getBlockEntityOrThrow(BlockGetter level, BlockPos pos, BlockEntityType<TBlockEntity> blockEntityType) {
        return Objects.requireNonNull(getBlockEntity(level, pos, blockEntityType));
    }

    static <TBlockEntity extends BlockEntity> TBlockEntity getBlockEntityOrThrow(BlockGetter level, BlockPos pos, Supplier<BlockEntityType<TBlockEntity>> blockEntityType) {
        return getBlockEntityOrThrow(level, pos, blockEntityType.get());
    }
}
