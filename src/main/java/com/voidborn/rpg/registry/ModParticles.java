package com.voidborn.rpg.registry;

import com.voidborn.rpg.VoidbornRpgMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, VoidbornRpgMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> VOID_SLASH =
            PARTICLE_TYPES.register("void_slash", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SHADOW_VORTEX =
            PARTICLE_TYPES.register("shadow_vortex", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
