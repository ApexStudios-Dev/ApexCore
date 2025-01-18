package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public interface DelegatedNoiseBiomeSource extends BiomeManager.NoiseBiomeSource {
    BiomeManager.NoiseBiomeSource delegate();

    @Override
    default Holder<Biome> getNoiseBiome(int x, int y, int z) {
        return delegate().getNoiseBiome(x, y, z);
    }
}
