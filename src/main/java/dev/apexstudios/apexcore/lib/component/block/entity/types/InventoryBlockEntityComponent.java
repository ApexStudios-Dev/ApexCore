package dev.apexstudios.apexcore.lib.component.block.entity.types;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.component.ComponentBuilder;
import dev.apexstudios.apexcore.lib.component.ComponentHolder;
import dev.apexstudios.apexcore.lib.component.ComponentType;
import dev.apexstudios.apexcore.lib.component.block.entity.BaseBlockEntityComponent;
import dev.apexstudios.apexcore.lib.component.block.entity.BlockEntityComponent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
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
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

public final class InventoryBlockEntityComponent extends BaseBlockEntityComponent {
    public static final ComponentType<BlockEntityComponent, InventoryBlockEntityComponent, Builder> COMPONENT_TYPE = ComponentType.registerBlockEntity(
            ApexCore.identifier("inventory"),
            Builder::new,
            InventoryBlockEntityComponent::new
    );

    public static final String NBT_INVENTORY = "Inventory";

    private static final ICapabilityProvider<? extends ComponentHolder<BlockEntityComponent>, @Nullable Direction, IItemHandler> CAPABILITY_PROVIDER = (holder, context) -> {
        var component = holder.getComponent(COMPONENT_TYPE);
        return component == null ? EmptyItemHandler.INSTANCE : component.getItemHandler();
    };

    private final boolean saveToItem;
    private final Inventory inventory;

    private InventoryBlockEntityComponent(ComponentHolder<BlockEntityComponent> holder, Builder builder) {
        super(holder);

        saveToItem = builder.saveToItem;
        inventory = new Inventory(builder);
    }

    public IItemHandlerModifiable getItemHandler() {
        return inventory;
    }

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(NBT_INVENTORY, inventory.serializeNBT(registries));
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if(tag.contains(NBT_INVENTORY, Tag.TAG_COMPOUND))
            inventory.deserializeNBT(registries, tag.getCompound(NBT_INVENTORY));
    }

    @Override
    public void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        var contents = input.get(DataComponents.CONTAINER);

        if(!saveToItem || contents == null)
            return;

        for(var i = 0; i < contents.getSlots() && i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, contents.getStackInSlot(i));
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        if(!saveToItem)
            return;

        var items = IntStream.range(0, inventory.getSlots()).mapToObj(inventory::getStackInSlot).map(ItemStack::copy).toList();
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        if(saveToItem)
            tag.remove(NBT_INVENTORY);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean movedByPiston) {
        if(blockState.is(newBlockState.getBlock()))
            return;

        var block = blockState.getBlock();

        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        for(var i = 0; i < inventory.getSlots(); i++) {
            Containers.dropItemStack(level, x, y, z, inventory.getStackInSlot(i));
        }

        level.updateNeighbourForOutputSignal(pos, block);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return ItemHandlerHelper.calcRedstoneFromInventory(inventory);
    }

    public static <TBlockEntity extends BlockEntity & ComponentHolder<BlockEntityComponent>> void registerCapability(BlockEntityType<TBlockEntity> blockEntityType, RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, blockEntityType, capability());
    }

    public static <TBlockEntity extends BlockEntity & ComponentHolder<BlockEntityComponent>> ICapabilityProvider<TBlockEntity, @Nullable Direction, IItemHandler> capability() {
        return (ICapabilityProvider<TBlockEntity, Direction, IItemHandler>) CAPABILITY_PROVIDER;
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

        public Builder slots(int count, ObjIntConsumer<SlotBuilder> consumer) {
            IntStream.range(0, count).forEach(index -> slot(index, builder -> consumer.accept(builder, index)));
            return this;
        }

        public Builder slots(int count, Consumer<SlotBuilder> consumer) {
            return slots(count, (builder, index) -> consumer.accept(builder));
        }

        public Builder slots(int count) {
            return slot(count, Consumers.nop());
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

    private static final class Inventory extends ItemStackHandler {
        private final int limit;
        private final Int2ObjectMap<SlotBuilder.Limit> slotLimit = new Int2ObjectOpenHashMap<>();
        private final Int2ObjectMap<SlotBuilder.Validator> slotValidator = new Int2ObjectOpenHashMap<>();
        private final Int2ObjectMap<SlotBuilder.Listener> slotListener = new Int2ObjectOpenHashMap<>();

        private Inventory(Builder builder) {
            super(builder.slots.size());

            limit = Math.max(builder.limit, Item.ABSOLUTE_MAX_STACK_SIZE);

            builder.slots.forEach((index, consumer) -> {
                var slotBuilder = new SlotBuilder();
                consumer.accept(slotBuilder);

                slotLimit.put(index, slotBuilder.limit);
                slotValidator.put(index, slotBuilder.validator);
                slotListener.put(index, slotBuilder.listener);
            });
        }

        @Override
        public int getSlotLimit(int slot) {
            return limit;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            var limit = slotLimit.get(slot);
            var baseLimit = super.getStackLimit(slot, stack);
            return limit == null ? baseLimit : Math.min(baseLimit, limit.limit(slot, this, stack));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            var test = slotValidator.get(slot);
            return test == null || test.isValid(slot, this, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            var listener = slotListener.get(slot);

            if(listener != null)
                listener.invoke(slot, this);
        }
    }
}
