package dev.apexstudios.apexcore.api.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public abstract class BlockTagsProvider extends net.neoforged.neoforge.common.data.BlockTagsProvider {
    protected BlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    public TagBuilder getOrCreateRawBuilder(@Nullable TagKey<Block> tag) {
        if(tag == null) {
            return TagBuilder.create();
        }

        return super.getOrCreateRawBuilder(tag);
    }

    @Override
    public TagAppender<Block> tag(@Nullable TagKey<Block> tag) {
        return super.tag(tag);
    }

    @Override
    public TagAppender<Block> tag(@Nullable TagKey<Block> tag, boolean replace) {
        return super.tag(tag, replace);
    }
}
