package com.voidborn.rpg.client;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.client.anim.PlayerAnimationHandler;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.C2SSpecialAttackPacket;
import com.voidborn.rpg.network.packet.C2SUltimateSkillPacket;
import com.voidborn.rpg.particles.ShadowVortexParticle;
import com.voidborn.rpg.particles.VoidSlashParticle;
import com.voidborn.rpg.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientModEvents {

    @Mod.EventBusSubscriber(modid = VoidbornRpgMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                PlayerAnimationHandler.registerPlayerAnimatorFactory();
                VoidbornRpgMod.LOGGER.info("[VoidbornRPG] ClientSetup y PlayerAnimator inicializados.");
            });
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(Keybindings.ULTIMATE_KEY);
            event.register(Keybindings.SPECIAL_SKILL_KEY);
        }

        @SubscribeEvent
        public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.VOID_SLASH.get(), VoidSlashParticle.Provider::new);
            event.registerSpriteSet(ModParticles.SHADOW_VORTEX.get(), ShadowVortexParticle.Provider::new);
        }
    }

    @Mod.EventBusSubscriber(modid = VoidbornRpgMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            CameraShakeHandler.tickClient();

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) {
                return;
            }

            while (Keybindings.ULTIMATE_KEY.consumeClick()) {
                PacketHandler.sendToServer(new C2SUltimateSkillPacket());
            }

            while (Keybindings.SPECIAL_SKILL_KEY.consumeClick()) {
                PacketHandler.sendToServer(new C2SSpecialAttackPacket(true));
            }
        }

        @SubscribeEvent
        public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            CameraShakeHandler.onComputeCameraAngles(event);
        }

        @SubscribeEvent
        public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
            PlayerAnimationHandler.applyProceduralCombatStance(event);
        }
    }
}
