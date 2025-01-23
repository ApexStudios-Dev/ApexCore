package dev.apexstudios.apexcore.mixin;

import java.util.Map;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StateDefinition.Builder.class)
public interface StateDefinitionBuilderAccessor {
    @Accessor("properties")
    Map<String, Property<?>> ApexCore$getProperties();
}
