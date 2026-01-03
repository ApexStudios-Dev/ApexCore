package dev.apexstudios.apexcore.client;

import com.mojang.serialization.MapCodec;
import dev.apexstudios.apexcore.api.block.Dyeable;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class DyeColorItemTintSource implements ItemTintSource {
    public static final MapCodec<DyeColorItemTintSource> MAP_CODEC = MapCodec.unit(DyeColorItemTintSource::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        Dyeable.DyedColor dyedColor;

        if(Block.byItem(stack.getItem()) instanceof Dyeable dyeable) {
            dyedColor = dyeable.getDyedColor(stack);
        } else {
            dyedColor = Dyeable.DyedColor.from(stack.get(DataComponents.BASE_COLOR));
        }

        var color = dyedColor.getColor();
        return color == null ? CommonColors.WHITE : color.getTextureDiffuseColor();
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
