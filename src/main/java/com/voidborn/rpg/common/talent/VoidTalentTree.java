package com.voidborn.rpg.common.talent;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class VoidTalentTree {
    public enum Branch {
        TITAN,
        SHADOW,
        ARCANIST
    }

    private Branch chosenBranch = null;
    private int talentPoints = 0;

    public boolean unlockBranch(ServerPlayer player, Branch branch) {
        if (this.chosenBranch != null && this.chosenBranch != branch) {
            return false;
        }

        this.chosenBranch = branch;
        PlayerCombatData.get(player).syncToClient(player);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 0.85F);
        return true;
    }

    public float getDamageReductionMultiplier() {
        return this.chosenBranch == Branch.TITAN ? 0.85F : 1.0F;
    }

    public float getManaCostMultiplier() {
        return this.chosenBranch == Branch.ARCANIST ? 0.75F : 1.0F;
    }

    public float getBonusArmorPenetration() {
        return this.chosenBranch == Branch.SHADOW ? 0.20F : 0.0F;
    }

    public void onTakeDamage(ServerPlayer player, float damageTaken) {
        if (this.chosenBranch == Branch.TITAN) {
            PlayerCombatData data = PlayerCombatData.get(player);
            float manaGain = Math.min(25.0F, damageTaken * 0.4F);
            data.applySyncFromServer(
                    Math.min(data.getMaxMana(), data.getCurrentMana() + manaGain),
                    data.getMaxMana(),
                    data.getSpecialCooldown(),
                    data.getUltimateCooldown(),
                    data.getComboStage(),
                    data.getUltimateCastTicks()
            );
            data.syncToClient(player);
        }
    }

    public void onKillEntity(ServerPlayer player) {
        if (this.chosenBranch == Branch.SHADOW) {
            PlayerCombatData data = PlayerCombatData.get(player);
            data.setSpecialCooldown(0);
            data.syncToClient(player);
            player.getCooldowns().removeCooldown(ModItems.SHADOW_DAGGERS.get());
        }
    }

    public void writeToNbt(CompoundTag tag) {
        if (this.chosenBranch != null) {
            tag.putString("VoidTalentBranch", this.chosenBranch.name());
        }
        tag.putInt("VoidTalentPoints", this.talentPoints);
    }

    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("VoidTalentBranch")) {
            try {
                this.chosenBranch = Branch.valueOf(tag.getString("VoidTalentBranch"));
            } catch (IllegalArgumentException ignored) {
                this.chosenBranch = null;
            }
        }
        this.talentPoints = tag.getInt("VoidTalentPoints");
    }

    public Branch getChosenBranch() { return chosenBranch; }
}
