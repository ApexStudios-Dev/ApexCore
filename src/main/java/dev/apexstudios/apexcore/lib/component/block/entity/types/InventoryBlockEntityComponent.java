package dev.apexstudios.apexcore.lib.component.block.entity.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.BaseBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

public final class InventoryBlockEntityComponent extends BaseBlockEntityComponent implements IItemHandlerModifiable {
    public static final ComponentType<BlockEntityComponent, InventoryBlockEntityComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlockEntity(
            ApexCore.identifier("inventory"),
            Builder::new,
            InventoryBlockEntityComponent::new
    );

    private static final ICapabilityProvider<? extends ComponentHolder<BlockEntityComponent>, @Nullable Direction, IItemHandler> CAPABILITY_PROVIDER = (holder, context) -> holder.getComponent(COMPONENT_TYPE);

    private final Slot[] slots;
    private final int limit;
    private final boolean saveToItem;

    private InventoryBlockEntityComponent(ComponentHolder<BlockEntityComponent> holder, Builder builder) {
        super(holder);

        limit = Math.max(builder.limit, 0);
        saveToItem = builder.saveToItem;

        slots = new Slot[builder.slots.size()];

        for(var i = 0; i < slots.length; i++) {
            var slotBuilder = new SlotBuilder();
            var action = builder.slots.get(i);

            if(action != null)
                action.accept(slotBuilder);

            slots[i] = new Slot(i, slotBuilder);
        }
    }

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        for(var slot : slots) {
            if(!slot.existing.isEmpty())
                tag.put(String.valueOf(slot.index), slot.existing.save(registries));
        }
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        for(var slot : slots) {
            var key = String.valueOf(slot.index);

            if(tag.contains(key, Tag.TAG_COMPOUND))
                slot.existing = ItemStack.parseOptional(registries, tag.getCompound(key));
            else
                slot.existing = ItemStack.EMPTY;
        }
    }

    @Override
    public void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        var contents = input.get(DataComponents.CONTAINER);

        if(!saveToItem || contents == null)
            return;

        for(var slot : slots) {
            slot.existing = contents.getStackInSlot(slot.index);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        if(!saveToItem)
            return;

        var items = Stream.of(slots).map(slot -> slot.existing).map(ItemStack::copy).toList();
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        if(!saveToItem)
            return;

        for(var slot : slots) {
            tag.remove(String.valueOf(slot.index));
        }
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean movedByPiston) {
        if(blockState.is(newBlockState.getBlock()))
            return;

        var block = blockState.getBlock();

        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        for(var stack : slots) {
            Containers.dropItemStack(level, x, y, z, stack.existing);
        }

        level.updateNeighbourForOutputSignal(pos, block);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return ItemHandlerHelper.calcRedstoneFromInventory(this);
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        LootTableBlockEntityComponent.unpack(asBlockEntity(), null);
        validateSlotIndex(index).set(stack);
    }

    @Override
    public int getSlots() {
        return slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        LootTableBlockEntityComponent.unpack(asBlockEntity(), null);
        return validateSlotIndex(index).existing.copy();
    }

    @Override
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
        LootTableBlockEntityComponent.unpack(asBlockEntity(), null);
        return validateSlotIndex(index).insert(stack, simulate);
    }

    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        LootTableBlockEntityComponent.unpack(asBlockEntity(), null);
        return validateSlotIndex(index).extract(amount, simulate);
    }

    @Override
    public int getSlotLimit(int index) {
        return limit;
    }

    @Override
    public boolean isItemValid(int index, ItemStack stack) {
        LootTableBlockEntityComponent.unpack(asBlockEntity(), null);
        return validateSlotIndex(index).isValid(stack);
    }

    private Slot validateSlotIndex(int index) {
        Objects.checkIndex(index, slots.length);
        return slots[index];
    }

    public static <TBlockEntity extends BlockEntity & ComponentHolder<BlockEntityComponent>> void registerCapability(BlockEntityType<TBlockEntity> blockEntityType, RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, blockEntityType, capability());
    }

    public static <TBlockEntity extends BlockEntity & ComponentHolder<BlockEntityComponent>> ICapabilityProvider<TBlockEntity, @Nullable Direction, IItemHandler> capability() {
        return (ICapabilityProvider<TBlockEntity, Direction, IItemHandler>) CAPABILITY_PROVIDER;
    }

    private final class Slot {
        private final int index;
        private final SlotBuilder.Limit limit;
        private final SlotBuilder.Validator validator;
        private final SlotBuilder.Listener listener;

        private ItemStack existing = ItemStack.EMPTY;

        private Slot(int index, SlotBuilder builder) {
            this.index = index;

            limit = builder.limit;
            validator = builder.validator;
            listener = builder.listener;
        }

        public void fireListener() {
            listener.invoke(index, InventoryBlockEntityComponent.this);
            asBlockEntity().setChanged();
        }

        public void set(ItemStack stack) {
            existing = stack.copy();
            fireListener();
        }

        public ItemStack insert(ItemStack stack, boolean simulate) {
            if(stack.isEmpty() || !isValid(stack))
                return ItemStack.EMPTY;

            var limit = Math.min(InventoryBlockEntityComponent.this.getSlotLimit(index), getLimit(stack));

            if(!existing.isEmpty()) {
                if(!ItemStack.isSameItemSameComponents(stack, existing))
                    return stack;

                limit -= existing.getCount();
            }

            if(limit <= 0)
                return stack;

            var reachedLimit = stack.getCount() > limit;

            if(!simulate) {
                if(existing.isEmpty())
                    existing = reachedLimit ? stack.copyWithCount(limit) : stack;
                else
                    existing.grow(reachedLimit ? limit : stack.getCount());

                fireListener();
            }

            return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
        }

        public ItemStack extract(int amount, boolean simulate) {
            if(amount <= 0 || existing.isEmpty())
                return ItemStack.EMPTY;

            var toExtract = Math.min(amount, existing.getMaxStackSize());
            var copy = existing.copy();

            if(existing.getCount() <= toExtract) {
                if(!simulate) {
                    existing = ItemStack.EMPTY;
                    fireListener();
                }

                return copy;
            }

            if(!simulate) {
                existing = existing.copyWithCount(existing.getCount() - toExtract);
                fireListener();
            }

            return copy.copyWithCount(toExtract);
        }

        public int getLimit(ItemStack stack) {
            return limit.limit(index, InventoryBlockEntityComponent.this, stack);
        }

        public boolean isValid(ItemStack stack) {
            return validator.isValid(index, InventoryBlockEntityComponent.this, stack);
        }
    }

    public static final class Builder implements ComponentBuilder {
        private int limit = Item.ABSOLUTE_MAX_STACK_SIZE;
        private final Int2ObjectMap<Consumer<SlotBuilder>> slots = new Int2ObjectOpenHashMap<>();
        private boolean saveToItem = false;

        public Builder limit(int limit) {
            this.limit = Math.max(limit, 1);
            return this;
        }

        public Builder slot(int index, Consumer<SlotBuilder> builder) {
            slots.merge(index, builder, Consumer::andThen);
            return this;
        }

        public Builder slot(int index) {
            return slot(index, Consumers.nop());
        }

        public Builder slots(int index, int... indices) {
            for(var slot : indices) {
                slot(slot);
            }

            return slot(index);
        }

        public Builder slots(int row, int col) {
            return slots(row * col);
        }

        public Builder slots(int count) {
            IntStream.range(0, count).forEach(this::slot);
            return this;
        }

        public Builder saveToItem() {
            saveToItem = true;
            return this;
        }
    }

    public static final class SlotBuilder {
        private Limit limit = (index, inventory, stack) -> stack.getMaxStackSize();
        private Validator validator = (index, inventory, stack) -> true;
        private Listener listener = (index, inventory) -> { };

        public SlotBuilder limit(int limit) {
            return limit((index, inventory, stack) -> limit);
        }

        public SlotBuilder limit(Limit limit) {
            this.limit = limit;
            return this;
        }

        public SlotBuilder validator(Validator validator, Validator.Merge merge) {
            this.validator = merge.merge(this.validator, validator);
            return this;
        }

        public SlotBuilder validator(Validator validator) {
            return validator(validator, Validator.Merge.AND);
        }

        public SlotBuilder listener(Listener listener) {
            this.listener = this.listener.andThen(listener);
            return this;
        }

        @FunctionalInterface
        public interface Limit {
            int limit(int index, IItemHandler inventory, ItemStack stack);
        }

        @FunctionalInterface
        public interface Validator {
            boolean isValid(int index, IItemHandler inventory, ItemStack stack);

            enum Merge {
                AND {
                    @Override
                    public Validator merge(Validator left, Validator right) {
                        return (index, inventory, stack) -> left.isValid(index, inventory, stack) && right.isValid(index, inventory, stack);
                    }
                },
                OR {
                    @Override
                    public Validator merge(Validator left, Validator right) {
                        return (index, inventory, stack) -> left.isValid(index, inventory, stack) || right.isValid(index, inventory, stack);
                    }
                };

                public abstract Validator merge(Validator left, Validator right);
            }
        }

        @FunctionalInterface
        public interface Listener {
            void invoke(int index, IItemHandlerModifiable inventory);

            default Listener andThen(Listener listener) {
                return (index, inventory) -> {
                    invoke(index, inventory);
                    listener.invoke(index, inventory);
                };
            }
        }
    }
}
