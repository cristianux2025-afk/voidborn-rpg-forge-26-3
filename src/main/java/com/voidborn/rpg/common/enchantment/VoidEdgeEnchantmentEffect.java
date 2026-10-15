package com.voidborn.rpg.common.enchantment;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoidEdgeEnchantmentEffect {
    private static final String MARK_TAG = "VoidbornMarkedTicks";

    public static void onPostAttack(ServerPlayer attacker, LivingEntity victim, int enchantmentLevel) {
        if (enchantmentLevel <= 0 || !victim.isAlive()) {
            return;
        }

        victim.getPersistentData().putInt(MARK_TAG, 80);

        ServerLevel level = attacker.serverLevel();
        level.sendParticles(ParticleTypes.WITCH,
                victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                8, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    public static void checkAndDetonateMark(ServerPlayer attacker, LivingEntity target, float skillDamageMultiplier) {
        if (!target.getPersistentData().contains(MARK_TAG)) {
            return;
        }

        target.getPersistentData().remove(MARK_TAG);
        ServerLevel level = attacker.serverLevel();
        Vec3 pos = target.position();

        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.6F, 1.3F);

        PacketHandler.sendToNear(level, pos, 36.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.CRITICAL_BURST,
                        pos.x, target.getY() + target.getBbHeight() * 0.5D, pos.z,
                        0.0D, 1.0D, 0.0D, 2.5F, 35
                ));

        float extraDamage = 12.0F * skillDamageMultiplier;
        AABB blastArea = target.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> nearbyEnemies = level.getEntitiesOfClass(LivingEntity.class, blastArea,
                e -> e != attacker && e.isAlive() && !attacker.isAlliedTo(e));

        for (LivingEntity enemy : nearbyEnemies) {
            enemy.hurt(level.damageSources().indirectMagic(attacker, attacker), extraDamage);
            enemy.hurtMarked = true;
        }

        PlayerCombatData data = PlayerCombatData.get(attacker);
        data.applySyncFromServer(
                Math.min(data.getMaxMana(), data.getCurrentMana() + 15.0F),
                data.getMaxMana(),
                data.getSpecialCooldown(),
                data.getUltimateCooldown(),
                data.getComboStage(),
                data.getUltimateCastTicks()
        );
        data.syncToClient(attacker);
    }
}
