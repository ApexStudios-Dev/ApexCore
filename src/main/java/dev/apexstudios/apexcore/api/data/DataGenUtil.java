package dev.apexstudios.apexcore.api.data;

import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.tags.BlockItemTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface DataGenUtil {
    static Component packDescription(String modId) {
        return Component.translatable("pack." + modId + ".desc");
    }

    static PackMetadataGenerator metadata(PackOutput output, Component description, PackType type) {
        return new PackMetadataGenerator(output)
                .add(PackMetadataSection.forPackType(type), new PackMetadataSection(
                        description,
                        DetectedVersion.BUILT_IN.packVersion(type).minorRange()
                ));
    }

    static PackMetadataGenerator metadata(PackOutput output, Component description, PackType type, FeatureFlagSet enabledFeatures) {
        return metadata(output, description, type)
                .add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(enabledFeatures));
    }

    static void blockItemTags(Function<Function<BlockItemTagId, BlockItemTagsProvider.CombinedAppender>, BlockItemTagsProvider> factory, TagsProvider<Block> provider) {
        blockItemTags$internal(factory, provider, BlockItemTagId::block, BlockItemId::block);
    }

    static void itemBlockItem(Function<Function<BlockItemTagId, BlockItemTagsProvider.CombinedAppender>, BlockItemTagsProvider> factory, TagsProvider<Item> provider) {
        blockItemTags$internal(factory, provider, BlockItemTagId::item, BlockItemId::item);
    }

    private static <TRegistry> void blockItemTags$internal(
            Function<Function<BlockItemTagId, BlockItemTagsProvider.CombinedAppender>, BlockItemTagsProvider> factory,
            TagsProvider<TRegistry> provider,
            Function<BlockItemTagId, TagKey<TRegistry>> tagMapper,
            Function<BlockItemId, ResourceKey<TRegistry>> valueMapper
    ) {
        factory.apply(tagId -> new BlockItemTagsProvider.CombinedAppender() {
            private final TagAppender<TRegistry> appender = TagAppender.forBuilder(provider.getOrCreateRawBuilder(tagMapper.apply(tagId)));

            @Override
            public BlockItemTagsProvider.CombinedAppender addAll(Stream<BlockItemId> ids) {
                appender.addAll(ids.map(valueMapper));
                return this;
            }

            @Override
            public BlockItemTagsProvider.CombinedAppender addTag(BlockItemTagId id) {
                appender.addTag(tagMapper.apply(id));
                return this;
            }
        }).run();
    }
}
