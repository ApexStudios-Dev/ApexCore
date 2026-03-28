package dev.apexstudios.apexcore.xplat.platform;

import java.util.Optional;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ModLoader {
    boolean isFabric();

    boolean isNeoForge();

    boolean isDevelopment();

    boolean isProduction();

    boolean isDataGen();

    boolean isClient();

    boolean isServer();

    boolean isLoaded(String modId);

    Optional<Mod> mod(String modId);

    Mod modOrThrow(String modId);

    static ModLoader get() {
        return ApexPlatform.INSTANCE.modLoader();
    }
}
