package dev.apexstudios.apexcore.api.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record TagPair(
        @Nullable TagKey<Block> block,
        @Nullable TagKey<Item> item
) { }
