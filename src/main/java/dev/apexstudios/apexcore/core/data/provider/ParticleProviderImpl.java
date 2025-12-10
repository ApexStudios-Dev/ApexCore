package dev.apexstudios.apexcore.core.data.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import dev.apexstudios.apexcore.lib.data.provider.ParticleProvider;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderOutputContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class ParticleProviderImpl implements BaseProvider, ParticleProvider {
    public static final ProviderType<ParticleProvider> PROVIDER_TYPE = ProviderType.register(ApexCore.identifier("particle"), ParticleProviderImpl::new);

    private final Map<Identifier, List<String>> descriptions = Maps.newHashMap();

    @Override
    public CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context) {
        var pathProvider = context.pathProvider(PackOutput.Target.RESOURCE_PACK, "particles");

        return DataProvider.saveAll(
                cache,
                desc -> Util.make(new JsonObject(), root -> root.add("textures", Util.make(new JsonArray(), json -> desc.forEach(json::add)))),
                pathProvider::json,
                descriptions
        );
    }

    @Override
    public void spriteSet(ParticleType<?> particleType, Iterable<Identifier> textures) {
        var registryName = Objects.requireNonNull(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType));
        var desc = Lists.<String>newArrayList();

        for(var texture : textures) {
            desc.add(texture.toString());
        }

        if(descriptions.putIfAbsent(registryName, desc) != null)
            throw new IllegalStateException("The particle type '" + registryName + "' already has a description associated with it");
    }
}
