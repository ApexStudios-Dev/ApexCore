package dev.apexstudios.apexcore.lib.data.provider;

import dev.apexstudios.apexcore.core.data.provider.ParticleProviderImpl;
import dev.apexstudios.apexcore.lib.data.ProviderType;
import java.util.Iterator;
import java.util.stream.Stream;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;

public interface ParticleProvider {
    ProviderType<ParticleProvider> PROVIDER_TYPE = ParticleProviderImpl.PROVIDER_TYPE;

    default void sprite(ParticleType<?> particleType, ResourceLocation texture) {
        spriteSet(particleType, texture);
    }

    default void spriteSet(ParticleType<?> particleType, ResourceLocation baseTexturePath, int numTextures, boolean reverse) {
        spriteSet(particleType, () -> new Iterator<>() {
            private int counter = 0;

            @Override
            public boolean hasNext() {
                return counter < numTextures;
            }

            @Override
            public ResourceLocation next() {
                var texture = baseTexturePath.withSuffix("_" + (reverse ? numTextures - counter - 1 : counter));
                counter++;
                return texture;
            }
        });
    }

    default void spriteSet(ParticleType<?> particleType, ResourceLocation texture, ResourceLocation... textures) {
        spriteSet(particleType, Stream.concat(Stream.of(texture), Stream.of(textures))::iterator);
    }

    void spriteSet(ParticleType<?> particleType, Iterable<ResourceLocation> textures);
}
