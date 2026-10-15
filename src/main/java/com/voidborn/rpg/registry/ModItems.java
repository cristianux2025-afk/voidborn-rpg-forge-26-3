package com.voidborn.rpg.registry;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.item.AbyssalEyeTalisman;
import com.voidborn.rpg.common.item.ShadowDaggersItem;
import com.voidborn.rpg.common.item.VoidCoreItem;
import com.voidborn.rpg.common.item.VoidElixirItem;
import com.voidborn.rpg.common.item.VoidGreatswordItem;
import com.voidborn.rpg.common.item.VoidOrbItem;
import com.voidborn.rpg.common.item.VoidReaperCloakItem;
import com.voidborn.rpg.common.item.VoidRingItem;
import com.voidborn.rpg.common.item.VoidRuneItem;
import com.voidborn.rpg.common.item.VoidScytheItem;
import com.voidborn.rpg.common.item.VoidSpearItem;
import com.voidborn.rpg.common.item.VoidSummonTomeItem;
import com.voidborn.rpg.common.item.VoidbornArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VoidbornRpgMod.MOD_ID);

    // Armas RPG 3D con GeckoLib
    public static final RegistryObject<Item> VOID_GREATSWORD = ITEMS.register("void_greatsword",
            () -> new VoidGreatswordItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).durability(2850).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> SHADOW_DAGGERS = ITEMS.register("shadow_daggers",
            () -> new ShadowDaggersItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).durability(2100).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> VOID_SCYTHE = ITEMS.register("void_scythe",
            () -> new VoidScytheItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).durability(2600).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> VOID_SPEAR = ITEMS.register("void_spear",
            () -> new VoidSpearItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).durability(2450).rarity(Rarity.EPIC).fireResistant()));

    // Runas del Vacío (v1.7)
    public static final RegistryObject<Item> VOID_RUNE_BLOOD = ITEMS.register("void_rune_blood",
            () -> new VoidRuneItem(VoidRuneItem.RuneType.ABYSSAL_BLOOD));
    public static final RegistryObject<Item> VOID_RUNE_ANNIHILATION = ITEMS.register("void_rune_annihilation",
            () -> new VoidRuneItem(VoidRuneItem.RuneType.ANNIHILATION));
    public static final RegistryObject<Item> VOID_RUNE_GRAVITATIONAL = ITEMS.register("void_rune_gravitational",
            () -> new VoidRuneItem(VoidRuneItem.RuneType.GRAVITATIONAL));

    // Consumibles, Reliquias y Tomos (v1.8 - v2.4)
    public static final RegistryObject<Item> VOID_ELIXIR = ITEMS.register("void_elixir",
            VoidElixirItem::new);

    public static final RegistryObject<Item> VOID_RING = ITEMS.register("void_ring",
            VoidRingItem::new);

    public static final RegistryObject<Item> VOID_SUMMON_TOME = ITEMS.register("void_summon_tome",
            VoidSummonTomeItem::new);

    public static final RegistryObject<Item> VOID_REAPER_CLOAK = ITEMS.register("void_reaper_cloak",
            VoidReaperCloakItem::new);

    public static final RegistryObject<Item> VOID_ORB = ITEMS.register("void_orb",
            () -> new VoidOrbItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> ABYSSAL_EYE_TALISMAN = ITEMS.register("abyssal_eye_talisman",
            AbyssalEyeTalisman::new);

    public static final RegistryObject<Item> VOID_CORE = ITEMS.register("void_core",
            VoidCoreItem::new);

    // Set Completo de Armadura 3D Voidborn
    public static final RegistryObject<Item> VOIDBORN_HELMET = ITEMS.register("voidborn_helmet",
            () -> new VoidbornArmorItem(ModArmorMaterials.VOIDBORN, ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1).durability(580).rarity(Rarity.EPIC).fireResistant(),
                    4.0D, 0.08D, 25.0D, 1.5D));

    public static final RegistryObject<Item> VOIDBORN_CHESTPLATE = ITEMS.register("voidborn_chestplate",
            () -> new VoidbornArmorItem(ModArmorMaterials.VOIDBORN, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1).durability(720).rarity(Rarity.EPIC).fireResistant(),
                    8.0D, 0.12D, 40.0D, 3.0D));

    public static final RegistryObject<Item> VOIDBORN_LEGGINGS = ITEMS.register("voidborn_leggings",
            () -> new VoidbornArmorItem(ModArmorMaterials.VOIDBORN, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1).durability(680).rarity(Rarity.EPIC).fireResistant(),
                    6.0D, 0.10D, 30.0D, 2.0D));

    public static final RegistryObject<Item> VOIDBORN_BOOTS = ITEMS.register("voidborn_boots",
            () -> new VoidbornArmorItem(ModArmorMaterials.VOIDBORN, ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1).durability(540).rarity(Rarity.EPIC).fireResistant(),
                    2.0D, 0.05D, 15.0D, 1.0D));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
