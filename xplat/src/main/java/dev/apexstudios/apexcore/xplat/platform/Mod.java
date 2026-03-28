package dev.apexstudios.apexcore.xplat.platform;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface Mod {
    String modId();

    String namespace();

    String displayName();

    String version();
}
