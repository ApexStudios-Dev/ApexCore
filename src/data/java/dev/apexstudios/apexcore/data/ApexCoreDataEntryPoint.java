package dev.apexstudios.apexcore.data;

import dev.apexstudios.apexcore.core.ApexCore;
import dev.apexstudios.apexcore.lib.data.ProviderTypes;
import dev.apexstudios.apexcore.lib.data.ResourceGenerator;
import dev.apexstudios.apexcore.lib.data.provider.context.ProviderListenerContext;
import dev.apexstudios.apexcore.lib.data.provider.tag.IntrusiveTagProvider;
import dev.apexstudios.apexcore.lib.placement.BlockPlacementRenderer;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = ApexCore.ID, dist = Dist.CLIENT)
public final class ApexCoreDataEntryPoint {
    public ApexCoreDataEntryPoint(IEventBus modBus) {
        ResourceGenerator.of(modBus, generator -> {
            generator.pack();

            generator.pack("visual_vanilla")
                    .description("Enables the Placement Visualizer for all of Vanilla Minecraft")
                    .providing(ProviderTypes.BLOCK_TAGS, (context, provider) -> {
                        addTagToNamespace(context, provider, BlockPlacementRenderer.BLOCK_WHITELIST, ResourceLocation.DEFAULT_NAMESPACE, block -> {
                            var item = block.asItem();
                            return item instanceof BlockItem || item instanceof BucketItem;
                        });
                    })
                    .providing(ProviderTypes.FLUID_TAGS, (context, provider) -> {
                        addTagToNamespace(context, provider, BlockPlacementRenderer.FLUID_WHITELIST, ResourceLocation.DEFAULT_NAMESPACE, fluid -> fluid.isSource(fluid.defaultFluidState()));
                    });
        });
    }

    private <TRegistry> void addTagToNamespace(ProviderListenerContext context, IntrusiveTagProvider<TRegistry> provider, TagKey<TRegistry> tag, String namespace, Predicate<TRegistry> filter) {
        var builder = provider.tag(tag);

        context.registries()
                .lookupOrThrow(tag.registry())
                .listElements()
                .filter(holder -> holder.key().location().getNamespace().equals(namespace))
                .map(Holder::value)
                .filter(filter)
                .forEach(builder::withElement);
    }
}
