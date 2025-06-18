package dev.apexstudios.apexcore.lib.transfer.resource.item;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public interface ItemUtil {
    static boolean isEmpty(ItemLike item) {
        return item.asItem() == Items.AIR;
    }
}
