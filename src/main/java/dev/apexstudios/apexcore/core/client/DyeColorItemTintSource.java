package dev.apexstudios.apexcore.core.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record DyeColorItemTintSource(DyeColor defaultColor) implements ItemTintSource {
    public static final MapCodec<DyeColorItemTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            DyeColor.CODEC.fieldOf("default").forGetter(DyeColorItemTintSource::defaultColor)
    ).apply(builder, DyeColorItemTintSource::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return stack.getOrDefault(DataComponents.BASE_COLOR, defaultColor).getTextureDiffuseColor();
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
