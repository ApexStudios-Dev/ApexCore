package dev.apexstudios.apexcore.xplat.platform;

import java.util.ServiceLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public interface ApexPlatform {
    ApexPlatform INSTANCE = get(ApexPlatform.class);

    ModLoader modLoader();

    static <T> T get(Class<T> type) {
        return ServiceLoader.load(type, type.getClassLoader()).findFirst().orElseThrow();
    }
}
