package dev.apexstudios.apexcore.lib.component.block.entity.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.BaseBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class LockBlockEntityComponent extends BaseBlockEntityComponent {
    public static final ComponentType<BlockEntityComponent, LockBlockEntityComponent, BlockEntity, ComponentBuilder> COMPONENT_TYPE = ComponentType.registerBlockEntity(
            ApexCore.identifier("lock"),
            LockBlockEntityComponent::new
    );

    private LockCode lockCode = LockCode.NO_LOCK;

    private LockBlockEntityComponent(ComponentHolder<BlockEntityComponent, BlockEntity> holder) {
        super(holder);
    }

    public boolean canAccess(ItemStack stack) {
        return lockCode.unlocksWith(stack);
    }

    public void setLockCode(LockCode lockCode) {
        if(!this.lockCode.equals(lockCode)) {
            this.lockCode = lockCode;
            unwrap().setChanged();
        }
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        lockCode = LockCode.fromTag(tag, registries);
    }

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        lockCode.addToTag(tag, registries);
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter getter) {
        lockCode = getter.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        if(!lockCode.equals(LockCode.NO_LOCK))
            builder.set(DataComponents.LOCK, lockCode);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(LockCode.TAG_LOCK);
    }

    public static boolean isLocked(BlockEntity blockEntity, Player player) {
        if(!(blockEntity instanceof ComponentHolder))
            return false;

        var component = ((ComponentHolder<BlockEntityComponent, BlockEntity>) blockEntity).getComponent(BlockEntityComponentTypes.LOCK);
        return component != null && !component.canAccess(player.getMainHandItem());
    }

    public static boolean isLocked(BlockGetter level, BlockPos pos, Player player) {
        var blockEntity = level.getBlockEntity(pos);

        if(blockEntity == null)
            return true;

        return isLocked(blockEntity, player);
    }
}
