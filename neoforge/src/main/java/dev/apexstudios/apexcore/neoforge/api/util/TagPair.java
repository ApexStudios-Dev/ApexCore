package dev.apexstudios.apexcore.neoforge.api.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public record TagPair(
        @Nullable TagKey<Block> block,
        @Nullable TagKey<Item> item
) { }
