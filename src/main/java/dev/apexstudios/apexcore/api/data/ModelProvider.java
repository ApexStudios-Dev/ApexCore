package dev.apexstudios.apexcore.api.data;

import dev.apexstudios.registree.api.Registree;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ModelProvider extends net.minecraft.client.data.models.ModelProvider {
    private final Registree registree;

    public ModelProvider(PackOutput output, Registree registree) {
        super(output, registree.namespace());

        this.registree = registree;
    }

    @Override
    protected abstract void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels);

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return registree.registrar(Registries.BLOCK).streamHolders();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return registree.registrar(Registries.ITEM).streamHolders();
    }
}
