package dev.apexstudios.apexcore.api.util;

import dev.apexstudios.apexcore.common.ApexCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public interface ApexTags {
    interface Blocks {
        TagKey<Block> SEAT_PER_BLOCK = ApexCore.REGISTREE.tag(Registries.BLOCK, "seat_per_block");
        TagKey<Block> SHEARS_EFFICIENT = ApexCore.REGISTREE.tag(Registries.BLOCK, "shears_efficient");

        private static void register() {

        }
    }

    interface EntityTypes {
        TagKey<EntityType<?>> SEAT_BLACKLIST = ApexCore.REGISTREE.tag(Registries.ENTITY_TYPE, "seat_blacklist");

        private static void register() {

        }
    }

    static void register() {
        Blocks.register();
        EntityTypes.register();
    }
}
