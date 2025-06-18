package dev.apexstudios.apexcore.lib.transfer.resource.item;

import dev.apexstudios.apexcore.lib.transfer.resource.IDataComponentHolderResource;
import dev.apexstudios.apexcore.lib.transfer.resource.IRegisteredResource;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class ItemResource implements IRegisteredResource<Item>, IDataComponentHolderResource.Prototyped {
    public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

    private final ItemStack innerStack;

    private ItemResource(ItemStack innerStack) {
        this.innerStack = innerStack;
    }

    public boolean is(ItemResource resource) {
        return is(resource.innerStack);
    }

    public boolean is(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(innerStack, stack);
    }

    public boolean isSameItem(ItemResource resource) {
        return is(resource.value());
    }

    public boolean isSameItem(ItemStack stack) {
        return ItemStack.isSameItem(innerStack, stack);
    }

    public int getMaxStackSize() {
        return innerStack.getMaxStackSize();
    }

    public ItemStack toStack(int count) {
        if(isEmpty())
            return ItemStack.EMPTY;

        return innerStack.copyWithCount(count);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    @Override
    public Item value() {
        return innerStack.getItem();
    }

    @Override
    public Holder<Item> holder() {
        return innerStack.getItemHolder();
    }

    @Override
    public boolean is(TagKey<Item> tag) {
        return innerStack.is(tag);
    }

    @Override
    public boolean is(Item value) {
        return innerStack.is(value);
    }

    @Override
    public boolean is(Predicate<Holder<Item>> test) {
        return innerStack.is(test);
    }

    @Override
    public boolean is(Holder<Item> holder) {
        return innerStack.is(holder);
    }

    @Override
    public boolean is(HolderSet<Item> holders) {
        return innerStack.is(holders);
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    @Override
    public boolean hasNonDefault(DataComponentType<?> componentType) {
        return innerStack.hasNonDefault(componentType);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> component) {
        return innerStack.get(component);
    }

    @Override
    public <T> Stream<T> getAllOfType(Class<? extends T> type) {
        return innerStack.getAllOfType(type);
    }

    @Override
    public <T> T getOrDefault(DataComponentType<? extends T> component, T defaultValue) {
        return innerStack.getOrDefault(component, defaultValue);
    }

    @Override
    public boolean has(DataComponentType<?> component) {
        return innerStack.has(component);
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    @Nullable
    @Override
    public <T> TypedDataComponent<T> getTyped(DataComponentType<T> component) {
        return innerStack.getTyped(component);
    }

    @Nullable
    @Override
    public <T> T get(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return innerStack.get(componentType);
    }

    @Override
    public <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> componentType, T value) {
        return innerStack.getOrDefault(componentType, value);
    }

    @Override
    public <T> boolean has(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return innerStack.has(componentType);
    }

    @Override
    public <T extends TooltipProvider> void addToTooltip(DataComponentType<T> type, Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        innerStack.addToTooltip(type, context, adder, flag);
    }

    @Override
    public <T extends TooltipProvider> void addToTooltip(Supplier<? extends DataComponentType<T>> type, Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        innerStack.addToTooltip(type, context, adder, flag);
    }

    @Override
    public DataComponentMap getPrototype() {
        return innerStack.getPrototype();
    }

    @Override
    public boolean isEmpty() {
        return innerStack.isEmpty();
    }

    public static ItemResource of(ItemLike item, DataComponentPatch patch) {
        if(ItemUtil.isEmpty(item))
            return EMPTY;

        var stack = new ItemStack(item);
        stack.applyComponents(patch);
        return new ItemResource(stack);
    }

    public static ItemResource of(ItemLike item) {
        return of(item, DataComponentPatch.EMPTY);
    }

    public static ItemResource of(Holder<Item> holder, DataComponentPatch patch) {
        return of(holder::value, patch);
    }

    public static ItemResource of(Holder<Item> holder) {
        return of(holder, DataComponentPatch.EMPTY);
    }

    public static ItemResource of(ItemStack stack) {
        return stack.isEmpty() ? EMPTY : new ItemResource(stack.copyWithCount(1));
    }
}
