package com.voidborn.rpg.registry;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.block.VoidAltarBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VoidbornRpgMod.MOD_ID);

    public static final RegistryObject<Block> VOID_ALTAR = BLOCKS.register("void_altar",
            () -> new VoidAltarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
                    .strength(8.0F, 1200.0F)
                    .lightLevel(state -> 9)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOID_ALTAR_ITEM = ModItems.ITEMS.register("void_altar",
            () -> new BlockItem(VOID_ALTAR.get(), new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
