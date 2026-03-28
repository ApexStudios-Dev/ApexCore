package dev.apexstudios.apexcore.neoforge.platform;

import com.google.common.collect.Maps;
import dev.apexstudios.apexcore.xplat.platform.Mod;
import dev.apexstudios.apexcore.xplat.platform.ModLoader;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jspecify.annotations.Nullable;

final class NeoForgeModLoader implements ModLoader {
    private final Map<String, Mod> mods = Maps.newHashMap();

    @Override
    public boolean isFabric() {
        return false;
    }

    @Override
    public boolean isNeoForge() {
        return true;
    }

    @Override
    public boolean isDevelopment() {
        return SharedConstants.IS_RUNNING_IN_IDE;
    }

    @Override
    public boolean isProduction() {
        return FMLEnvironment.isProduction();
    }

    @Override
    public boolean isDataGen() {
        return DatagenModLoader.isRunningDataGen();
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public boolean isServer() {
        return FMLEnvironment.getDist().isDedicatedServer();
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
            var modInfo = findModInfo(modId);

            if(modInfo != null) {
                mod = new NeoForgeMod(modInfo);
                mods.put(modId, mod);
            }
        }

        return mod;
    }

    private static @Nullable IModInfo findModInfo(String modId) {
        try {
            var mods = ModList.get();

            if(mods != null) {
                return mods.getModContainerById(modId)
                        .map(ModContainer::getModInfo)
                        .orElse(null);
            }

            var loader = FMLLoader.getCurrentOrNull();

            if(loader == null) {
                return null;
            }

            return loader.getLoadingModList()
                    .getMods()
                    .stream()
                    .filter(mod -> mod.getModId().equals(modId))
                    .findFirst()
                    .orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }
}
