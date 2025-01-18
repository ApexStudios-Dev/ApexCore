package dev.apexstudios.apexcore.core.data.provider;

import dev.apexstudios.apexcore.lib.data.provider.context.ProviderOutputContext;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;

public interface BaseProvider {
    CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context);
}
