package dev.apexstudios.apexcore.api.util;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

public final class ItemTemplateBuilder {
    private final ItemLike item;
    private int count = 1;
    private final DataComponentPatch.Builder patch = DataComponentPatch.builder();

    private ItemTemplateBuilder(ItemLike item) {
        this.item = item;
    }

    public ItemTemplateBuilder count(int count) {
        this.count = count;
        return this;
    }

    public <TValue> ItemTemplateBuilder with(DataComponentType<TValue> type, TValue value) {
        patch.set(type, value);
        return this;
    }

    public <TValue> ItemTemplateBuilder with(Supplier<? extends DataComponentType<TValue>> type, TValue value) {
        return with(type.get(), value);
    }

    public <TValue> ItemTemplateBuilder with(TypedDataComponent<TValue> component) {
        patch.set(component);
        return this;
    }

    public <TValue> ItemTemplateBuilder without(DataComponentType<TValue> type) {
        patch.remove(type);
        return this;
    }

    public <TValue> ItemTemplateBuilder without(Supplier<? extends DataComponentType<TValue>> type) {
        return without(type.get());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ItemTemplateBuilder with(DataComponentPatch patch) {
        for(var entry : patch.entrySet()) {
            var type = entry.getKey();
            entry.getValue().ifPresentOrElse(
                    value -> with((DataComponentType) type, value),
                    () -> without(type)
            );
        }

        return this;
    }

    public ItemTemplateBuilder copy(ItemStackTemplate template) {
        return count(template.count()).with(template.components());
    }

    public ItemStackTemplate build() {
        return new ItemStackTemplate(
                // no `Item, int, DataComponentPatch` ctor exists, must extract the holder ourselves
                item.asItem().builtInRegistryHolder(),
                Math.max(1, count), // templates do not support <= 0
                patch.build()
        );
    }

    public static ItemTemplateBuilder from(ItemLike item) {
        return new ItemTemplateBuilder(item);
    }

    public static ItemTemplateBuilder from(ItemStackTemplate template) {
        return from(() -> template.item().value()).copy(template);
    }
}
