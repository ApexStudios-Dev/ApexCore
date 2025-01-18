package dev.apexstudios.apexcore.lib.util;

import net.minecraft.server.packs.repository.PackSource;

public interface ApexPackSources {
    PackSource BUILT_IN_NOT_AUTO = wrapping(PackSource.BUILT_IN, false);

    private static PackSource wrapping(PackSource original, boolean shouldAddAutomaticlly) {
        return PackSource.create(original::decorate, shouldAddAutomaticlly);
    }
}
