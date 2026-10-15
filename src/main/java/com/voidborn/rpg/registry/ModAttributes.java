package com.voidborn.rpg.registry;

import com.voidborn.rpg.VoidbornRpgMod;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, VoidbornRpgMod.MOD_ID);

    public static final RegistryObject<Attribute> ARMOR_PENETRATION = ATTRIBUTES.register("armor_penetration",
            () -> new RangedAttribute("attribute.name.voidbornrpg.armor_penetration", 0.0D, 0.0D, 1.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAX_MANA = ATTRIBUTES.register("max_mana",
            () -> new RangedAttribute("attribute.name.voidbornrpg.max_mana", 100.0D, 0.0D, 10000.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MANA_REGEN = ATTRIBUTES.register("mana_regen",
            () -> new RangedAttribute("attribute.name.voidbornrpg.mana_regen", 2.5D, 0.0D, 500.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CRIT_MULTIPLIER = ATTRIBUTES.register("crit_multiplier",
            () -> new RangedAttribute("attribute.name.voidbornrpg.crit_multiplier", 1.5D, 1.0D, 10.0D).setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
