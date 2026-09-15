package dev.apexstudios.apexcore.common.data.pack;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;

@FunctionalInterface
public interface RegistryPatcher {
    CompletableFuture<RegistrySetBuilder.PatchedRegistries> patch(CompletableFuture<HolderLookup.Provider> vanilla, RegistrySetBuilder modded);
}
