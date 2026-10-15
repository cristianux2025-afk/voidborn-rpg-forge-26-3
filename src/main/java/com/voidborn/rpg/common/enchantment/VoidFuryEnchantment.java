package com.voidborn.rpg.common.enchantment;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class VoidFuryEnchantment {
    private static final Random RANDOM = new Random();

    public static void applyCombatProc(ServerPlayer attacker, LivingEntity victim, int enchantmentLevel) {
        if (enchantmentLevel <= 0) return;

        float procChance = 0.15F + (enchantmentLevel - 1) * 0.10F;
        if (RANDOM.nextFloat() > procChance) return;

        ServerLevel level = attacker.serverLevel();
        PlayerCombatData data = PlayerCombatData.get(attacker);

        float manaRestore = 4.0F + (enchantmentLevel - 1) * 3.0F;
        data.applySyncFromServer(
                Math.min(data.getMaxMana(), data.getCurrentMana() + manaRestore),
                data.getMaxMana(),
                Math.max(0, data.getSpecialCooldown() - (enchantmentLevel * 10)),
                data.getUltimateCooldown(),
                data.getComboStage(),
                data.getUltimateCastTicks()
        );
        data.syncToClient(attacker);

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.4F, 1.3F);

        PacketHandler.sendToNear(level, victim.position(), 32.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.CLEAVE_ARC,
                        victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                        attacker.getLookAngle().x, attacker.getLookAngle().y, attacker.getLookAngle().z,
                        2.2F, 18
                ));

        if (enchantmentLevel >= 3 && victim instanceof Player targetPlayer) {
            if (targetPlayer.isBlocking()) {
                targetPlayer.disableShield();
            }
        }
    }
}
