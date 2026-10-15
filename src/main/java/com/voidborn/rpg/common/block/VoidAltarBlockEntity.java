package com.voidborn.rpg.common.block;

import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CCameraShakePacket;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import com.voidborn.rpg.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class VoidAltarBlockEntity extends BlockEntity {
    public static final String AWAKENED_TAG = "VoidAwakened";
    private ItemStack storedWeapon = ItemStack.EMPTY;
    private int echoShardsDeposited = 0;
    private final AtomicBoolean isRitualActive = new AtomicBoolean(false);

    public VoidAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_ALTAR.get(), pos, state);
    }

    public ItemStack getStoredWeapon() { return this.storedWeapon; }
    public int getEchoShardsDeposited() { return this.echoShardsDeposited; }

    public synchronized ItemInteractionResult handleItemInteraction(ServerPlayer player, InteractionHand hand, ItemStack heldItem) {
        ServerLevel level = (ServerLevel) this.level;
        if (level == null || this.isRitualActive.get()) return ItemInteractionResult.FAIL;

        if (this.storedWeapon.isEmpty()) {
            if (heldItem.getItem() instanceof SwordItem) {
                this.storedWeapon = heldItem.split(1);
                level.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.2F, 0.9F);
                setChanged();
                level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
                player.displayClientMessage(Component.translatable("message.voidbornrpg.altar_weapon_placed").withStyle(ChatFormatting.DARK_PURPLE), true);
                return ItemInteractionResult.SUCCESS;
            }
        } else {
            if (heldItem.is(Items.ECHO_SHARD) && this.echoShardsDeposited < 3) {
                heldItem.shrink(1);
                this.echoShardsDeposited++;
                level.playSound(null, this.worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.5F, 1.3F);

                if (this.echoShardsDeposited >= 3 && this.isRitualActive.compareAndSet(false, true)) {
                    try {
                        performAwakeningRitual(player, level);
                    } finally {
                        this.isRitualActive.set(false);
                    }
                } else {
                    player.displayClientMessage(Component.literal("• Infusión: " + this.echoShardsDeposited + "/3 Fragmentos de Eco").withStyle(ChatFormatting.AQUA), true);
                }
                setChanged();
                level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
                return ItemInteractionResult.SUCCESS;
            } else if (heldItem.isEmpty()) {
                player.setItemInHand(hand, this.storedWeapon);
                this.storedWeapon = ItemStack.EMPTY;
                this.echoShardsDeposited = 0;
                level.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.2F, 1.1F);
                setChanged();
                level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void performAwakeningRitual(ServerPlayer player, ServerLevel level) {
        CompoundTag tag = this.storedWeapon.getOrCreateTag();
        tag.putBoolean(AWAKENED_TAG, true);
        this.echoShardsDeposited = 0;

        level.playSound(null, this.worldPosition, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 2.0F, 0.6F);
        PacketHandler.sendToNear(level, this.worldPosition.getCenter(), 48.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.ULTIMATE_DETONATION_SHOCKWAVE,
                        this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 1.2D, this.worldPosition.getZ() + 0.5D,
                        0.0D, 1.0D, 0.0D, 5.0F, 80
                ));
        PacketHandler.sendToNear(level, this.worldPosition.getCenter(), 24.0D,
                new S2CCameraShakePacket(this.worldPosition.getCenter(), 20.0F, 0.6F, 15));

        player.displayClientMessage(Component.translatable("message.voidbornrpg.altar_awakened").withStyle(ChatFormatting.GOLD), true);
    }

    // v2.3: Sincronización de paquetes a clientes remotos
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.storedWeapon.isEmpty()) {
            tag.put("StoredWeapon", this.storedWeapon.save(registries));
        }
        tag.putInt("EchoShards", this.echoShardsDeposited);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StoredWeapon")) {
            this.storedWeapon = ItemStack.parseOptional(registries, tag.getCompound("StoredWeapon"));
        } else {
            this.storedWeapon = ItemStack.EMPTY;
        }
        this.echoShardsDeposited = tag.getInt("EchoShards");
    }
}
