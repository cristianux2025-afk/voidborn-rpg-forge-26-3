package com.voidborn.rpg.registry;

import com.voidborn.rpg.VoidbornRpgMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VoidbornRpgMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VOIDBORN_TAB = CREATIVE_TABS.register("voidborn_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.voidbornrpg.voidborn_tab"))
                    .icon(() -> new ItemStack(ModItems.VOID_GREATSWORD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.VOID_GREATSWORD.get());
                        output.accept(ModItems.SHADOW_DAGGERS.get());
                        output.accept(ModItems.VOID_SCYTHE.get());
                        output.accept(ModItems.VOID_SPEAR.get());
                        output.accept(ModItems.VOID_RUNE_BLOOD.get());
                        output.accept(ModItems.VOID_RUNE_ANNIHILATION.get());
                        output.accept(ModItems.VOID_RUNE_GRAVITATIONAL.get());
                        output.accept(ModItems.VOID_ELIXIR.get());
                        output.accept(ModItems.VOID_RING.get());
                        output.accept(ModItems.VOID_SUMMON_TOME.get());
                        output.accept(ModItems.VOID_REAPER_CLOAK.get());
                        output.accept(ModItems.VOID_ORB.get());
                        output.accept(ModItems.ABYSSAL_EYE_TALISMAN.get());
                        output.accept(ModItems.VOID_CORE.get());
                        output.accept(ModItems.VOIDBORN_HELMET.get());
                        output.accept(ModItems.VOIDBORN_CHESTPLATE.get());
                        output.accept(ModItems.VOIDBORN_LEGGINGS.get());
                        output.accept(ModItems.VOIDBORN_BOOTS.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
