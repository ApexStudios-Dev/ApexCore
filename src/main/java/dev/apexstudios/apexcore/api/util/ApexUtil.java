package dev.apexstudios.apexcore.api.util;

import dev.apexstudios.apexcore.common.ApexCore;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface ApexUtil {
    static boolean hasModifierKeyPressed(@Nullable Player player) {
        return player != null && player.getData(ApexCore.PLAYER_MODIFIER);
    }
}
