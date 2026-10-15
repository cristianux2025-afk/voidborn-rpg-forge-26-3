package com.voidborn.rpg.common.capability;

import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CSyncCombatDataPacket;
import com.voidborn.rpg.registry.ModAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCombatData {
    private static final String NBT_KEY = "VoidbornCombatData";
    private static final Map<UUID, PlayerCombatData> SERVER_CACHE = new ConcurrentHashMap<>();
    private static final PlayerCombatData CLIENT_INSTANCE = new PlayerCombatData();

    private float currentMana = 100.0F;
    private float maxMana = 100.0F;
    private int specialCooldown = 0;
    private int ultimateCooldown = 0;
    private int comboStage = 0;
    private int comboExpireTicks = 0;
    private int ultimateCastTicks = 0;

    public static PlayerCombatData get(Player player) {
        if (player.level().isClientSide()) {
            return CLIENT_INSTANCE;
        }
        return SERVER_CACHE.computeIfAbsent(player.getUUID(), uuid -> {
            PlayerCombatData data = new PlayerCombatData();
            data.loadFromPlayerPersistentData(player);
            return data;
        });
    }

    public static void removeServerCache(UUID uuid) {
        SERVER_CACHE.remove(uuid);
    }

    public void tickServer(ServerPlayer player) {
        boolean dirty = false;

        AttributeInstance maxManaAttr = player.getAttribute(ModAttributes.MAX_MANA.get());
        float computedMaxMana = maxManaAttr != null ? (float) maxManaAttr.getValue() : 100.0F;
        if (Math.abs(computedMaxMana - this.maxMana) > 0.01F) {
            this.maxMana = computedMaxMana;
            this.currentMana = Math.min(this.currentMana, this.maxMana);
            dirty = true;
        }

        AttributeInstance regenAttr = player.getAttribute(ModAttributes.MANA_REGEN.get());
        float regenPerTick = (regenAttr != null ? (float) regenAttr.getValue() : 2.5F) / 20.0F;
        if (this.currentMana < this.maxMana) {
            this.currentMana = Mth.clamp(this.currentMana + regenPerTick, 0.0F, this.maxMana);
            if (player.tickCount % 10 == 0) {
                dirty = true;
            }
        }

        if (this.specialCooldown > 0) {
            this.specialCooldown--;
            if (this.specialCooldown == 0 || this.specialCooldown % 5 == 0) {
                dirty = true;
            }
        }

        if (this.ultimateCooldown > 0) {
            this.ultimateCooldown--;
            if (this.ultimateCooldown == 0 || this.ultimateCooldown % 10 == 0) {
                dirty = true;
            }
        }

        if (this.comboExpireTicks > 0) {
            this.comboExpireTicks--;
            if (this.comboExpireTicks == 0 && this.comboStage != 0) {
                this.comboStage = 0;
                dirty = true;
            }
        }

        if (this.ultimateCastTicks > 0) {
            this.ultimateCastTicks--;
            dirty = true;
        }

        if (dirty) {
            saveToPlayerPersistentData(player);
            syncToClient(player);
        }
    }

    public boolean consumeMana(float amount) {
        if (this.currentMana < amount) {
            return false;
        }
        this.currentMana -= amount;
        return true;
    }

    public int advanceCombo(int maxStages, int windowTicks) {
        this.comboStage = (this.comboStage % maxStages) + 1;
        this.comboExpireTicks = windowTicks;
        return this.comboStage;
    }

    public void syncToClient(ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new S2CSyncCombatDataPacket(
                this.currentMana,
                this.maxMana,
                this.specialCooldown,
                this.ultimateCooldown,
                this.comboStage,
                this.ultimateCastTicks
        ));
    }

    public void applySyncFromServer(
            float currentMana,
            float maxMana,
            int specialCooldown,
            int ultimateCooldown,
            int comboStage,
            int ultimateCastTicks
    ) {
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.specialCooldown = specialCooldown;
        this.ultimateCooldown = ultimateCooldown;
        this.comboStage = comboStage;
        this.ultimateCastTicks = ultimateCastTicks;
    }

    public void saveToPlayerPersistentData(Player player) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag tag = new CompoundTag();
        tag.putFloat("CurrentMana", this.currentMana);
        tag.putFloat("MaxMana", this.maxMana);
        tag.putInt("SpecialCooldown", this.specialCooldown);
        tag.putInt("UltimateCooldown", this.ultimateCooldown);
        tag.putInt("ComboStage", this.comboStage);
        persistent.put(NBT_KEY, tag);
    }

    public void loadFromPlayerPersistentData(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(NBT_KEY, CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = persistent.getCompound(NBT_KEY);
            this.currentMana = tag.getFloat("CurrentMana");
            this.maxMana = Math.max(100.0F, tag.getFloat("MaxMana"));
            this.specialCooldown = tag.getInt("SpecialCooldown");
            this.ultimateCooldown = tag.getInt("UltimateCooldown");
            this.comboStage = tag.getInt("ComboStage");
        }
    }

    public float getCurrentMana() { return currentMana; }
    public float getMaxMana() { return maxMana; }
    public int getSpecialCooldown() { return specialCooldown; }
    public void setSpecialCooldown(int ticks) { this.specialCooldown = Math.max(0, ticks); }
    public int getUltimateCooldown() { return ultimateCooldown; }
    public void setUltimateCooldown(int ticks) { this.ultimateCooldown = Math.max(0, ticks); }
    public int getComboStage() { return comboStage; }
    public boolean isCastingUltimate() { return ultimateCastTicks > 0; }
    public int getUltimateCastTicks() { return ultimateCastTicks; }
    public void setUltimateCastTicks(int ticks) { this.ultimateCastTicks = Math.max(0, ticks); }
}
