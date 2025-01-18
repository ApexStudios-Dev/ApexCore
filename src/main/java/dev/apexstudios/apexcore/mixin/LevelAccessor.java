package dev.apexstudios.apexcore.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Level.class)
public interface LevelAccessor {
    @Accessor("isClientSide")
    @Mutable
    void ApexCore$setIsClientSide(boolean isClientSide);

    @Invoker("tickBlockEntities")
    void ApexCore$tickBlockEntities();

    @Invoker("prepareWeather")
    void ApexCore$prepareWeather();

    @Invoker("getEntities")
    LevelEntityGetter<Entity> ApexCore$getEntities();

    @Invoker("advanceDaytime")
    long ApexCore$advanceDaytime();
}
