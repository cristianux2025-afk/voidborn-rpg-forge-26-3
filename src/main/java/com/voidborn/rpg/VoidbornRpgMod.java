package com.voidborn.rpg;

import com.mojang.logging.LogUtils;
import com.voidborn.rpg.common.event.ServerCombatEvents;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.registry.ModAttributes;
import com.voidborn.rpg.registry.ModCreativeTabs;
import com.voidborn.rpg.registry.ModItems;
import com.voidborn.rpg.registry.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(VoidbornRpgMod.MOD_ID)
public class VoidbornRpgMod {
    public static final String MOD_ID = "voidbornrpg";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VoidbornRpgMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        GeckoLib.initialize();

        ModAttributes.register(modEventBus);
        ModItems.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::modifyEntityAttributes);

        MinecraftForge.EVENT_BUS.register(new ServerCombatEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.info("[VoidbornRPG] Canal de red SimpleChannel inicializado correctamente.");
        });
    }

    private void modifyEntityAttributes(final EntityAttributeModificationEvent event) {
        event.add(net.minecraft.world.entity.EntityType.PLAYER, ModAttributes.ARMOR_PENETRATION.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, ModAttributes.MAX_MANA.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, ModAttributes.MANA_REGEN.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, ModAttributes.CRIT_MULTIPLIER.get());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
