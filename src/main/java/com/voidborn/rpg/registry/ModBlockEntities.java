package com.voidborn.rpg.registry;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.block.VoidAltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VoidbornRpgMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<VoidAltarBlockEntity>> VOID_ALTAR =
            BLOCK_ENTITIES.register("void_altar",
                    () -> BlockEntityType.Builder.of(VoidAltarBlockEntity::new, ModBlocks.VOID_ALTAR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
