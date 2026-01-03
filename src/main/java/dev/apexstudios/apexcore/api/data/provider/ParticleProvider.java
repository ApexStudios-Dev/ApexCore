package dev.apexstudios.apexcore.api.data.provider;

import dev.apexstudios.apexcore.common.data.provider.ParticleProviderImpl;
import dev.apexstudios.apexcore.api.data.ProviderType;
import java.util.Iterator;
import java.util.stream.Stream;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;

public interface ParticleProvider {
    ProviderType<ParticleProvider> PROVIDER_TYPE = ParticleProviderImpl.PROVIDER_TYPE;

    default void sprite(ParticleType<?> particleType, Identifier texture) {
        spriteSet(particleType, texture);
    }

    default void spriteSet(ParticleType<?> particleType, Identifier baseTexturePath, int numTextures, boolean reverse) {
        spriteSet(particleType, () -> new Iterator<>() {
            private int counter = 0;

            @Override
            public boolean hasNext() {
                return counter < numTextures;
            }

            @Override
            public Identifier next() {
                var texture = baseTexturePath.withSuffix("_" + (reverse ? numTextures - counter - 1 : counter));
                counter++;
                return texture;
            }
        });
    }

    default void spriteSet(ParticleType<?> particleType, Identifier texture, Identifier... textures) {
        spriteSet(particleType, Stream.concat(Stream.of(texture), Stream.of(textures))::iterator);
    }

    void spriteSet(ParticleType<?> particleType, Iterable<Identifier> textures);
}
