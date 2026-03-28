package dev.apexstudios.apexcore.fabric.platform;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.xplat.platform.Mod;
import dev.apexstudios.apexcore.xplat.platform.ModLoader;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jspecify.annotations.Nullable;

final class FabricModLoader implements ModLoader {
    private final Map<String, Mod> mods = Maps.newHashMap();

    @Override
    public boolean isFabric() {
        return true;
    }

    @Override
    public boolean isNeoForge() {
        return false;
    }

    @Override
    public boolean isDevelopment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isProduction() {
        return !isDevelopment();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean isDataGen() {
        return FabricDataGenHelper.ENABLED;
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public boolean isLoaded(String modId) {
        return findMod(modId) != null;
    }

    @Override
    public Optional<Mod> mod(String modId) {
        return Optional.ofNullable(findMod(modId));
    }

    @Override
    public Mod modOrThrow(String modId) {
        return Objects.requireNonNull(findMod(modId));
    }

    private @Nullable Mod findMod(String modId) {
        var mod = mods.get(modId);

        if(mod == null) {
            var modContainer = findModContainer(modId);

            if(modContainer != null) {
                mod = new FabricMod(modContainer);
                mods.put(modId, mod);
            }
        }

        return mod;
    }

    private static @Nullable ModContainer findModContainer(String modId) {
        try {
            return FabricLoader.getInstance().getModContainer(modId).orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }
}
