package dev.apexstudios.apexcore.api.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import org.jspecify.annotations.Nullable;

public abstract class ItemTagsProvider extends BlockTagCopyingItemTagProvider {
    protected ItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, String modId) {
        super(output, lookupProvider, blockTags, modId);
    }

    // TODO: Remove once https://github.com/neoforged/NeoForge/pull/3609 is merged
    public void copy(@Nullable BlockItemTagId tag) {
        if(tag != null) {
            copy(tag.block(), tag.item());
        }
    }

    @Override
    protected void copy(@Nullable TagKey<Block> blockTag, @Nullable TagKey<Item> itemTag) {
        if(blockTag != null && itemTag != null) {
            super.copy(blockTag, itemTag);
        }
    }

    @Override
    public TagBuilder getOrCreateRawBuilder(@Nullable TagKey<Item> tag) {
        if(tag == null) {
            return TagBuilder.create();
        }

        return super.getOrCreateRawBuilder(tag);
    }

    @Override
    public TagAppender<Item> tag(@Nullable TagKey<Item> tag) {
        return super.tag(tag);
    }

    @Override
    public TagAppender<Item> tag(@Nullable TagKey<Item> tag, boolean replace) {
        return super.tag(tag, replace);
    }
}
