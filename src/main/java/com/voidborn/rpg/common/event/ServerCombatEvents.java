package com.voidborn.rpg.common.event;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.common.item.VoidbornArmorItem;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CCameraShakePacket;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import com.voidborn.rpg.registry.ModAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.combat.CombatRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ServerCombatEvents {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }

        ServerPlayer serverPlayer = (ServerPlayer) event.player;
        PlayerCombatData data = PlayerCombatData.get(serverPlayer);
        int castBeforeTick = data.getUltimateCastTicks();

        data.tickServer(serverPlayer);

        if (castBeforeTick == 1 && data.getUltimateCastTicks() == 0) {
            detonateVoidCataclysmUltimate(serverPlayer);
        }
    }

    private void detonateVoidCataclysmUltimate(ServerPlayer caster) {
        ServerLevel level = caster.serverLevel();
        Vec3 center = caster.position();
        double radius = 9.0D;

        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.5F, 0.7F);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0F, 0.65F);

        PacketHandler.sendToNear(level, center, 64.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.ULTIMATE_DETONATION_SHOCKWAVE,
                        center.x, center.y + 0.2D, center.z,
                        0.0D, 1.0D, 0.0D,
                        (float) radius, 140
                ));

        PacketHandler.sendToNear(level, center, 40.0D,
                new S2CCameraShakePacket(center, 36.0F, 1.15F, 30));

        float setBonusMultiplier = VoidbornArmorItem.hasFullVoidbornSet(caster) ? 1.25F : 1.0F;
        double baseAttack = caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float totalUltimateDamage = (float) ((34.0D + baseAttack * 2.2D) * setBonusMultiplier);

        AABB hitBox = caster.getBoundingBox().inflate(radius, 4.5D, radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox,
                entity -> entity != caster && entity.isAlive() && !caster.isAlliedTo(entity));

        for (LivingEntity target : targets) {
            double dist = target.position().distanceTo(center);
            if (dist <= radius) {
                float distanceFalloff = (float) Mth.clamp(1.15D - (dist / (radius * 1.4D)), 0.45D, 1.0D);
                target.hurt(level.damageSources().playerAttack(caster), totalUltimateDamage * distanceFalloff);

                Vec3 dir = target.position().subtract(center).normalize();
                double kbStrength = 1.65D * distanceFalloff;
                target.setDeltaMovement(
                        dir.x * kbStrength,
                        0.68D * distanceFalloff,
                        dir.z * kbStrength
                );
                target.hurtMarked = true;
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }

        LivingEntity victim = event.getEntity();
        float originalDamage = event.getAmount();
        float finalDamage = originalDamage;

        AttributeInstance armorPenAttr = attacker.getAttribute(ModAttributes.ARMOR_PENETRATION.get());
        double armorPenetration = armorPenAttr != null ? Mth.clamp(armorPenAttr.getValue(), 0.0D, 1.0D) : 0.0D;

        if (armorPenetration > 0.0D && victim.getArmorValue() > 0) {
            float totalArmor = (float) victim.getArmorValue();
            float toughness = (float) victim.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float effectiveArmor = totalArmor * (1.0F - (float) armorPenetration);

            float damageAfterStandardArmor = CombatRules.getDamageAfterAbsorb(
                    originalDamage, event.getSource(), totalArmor, toughness);
            float damageAfterPenetratedArmor = CombatRules.getDamageAfterAbsorb(
                    originalDamage, event.getSource(), effectiveArmor, toughness * (1.0F - (float) armorPenetration));

            if (damageAfterStandardArmor > 0.01F) {
                float compensationRatio = damageAfterPenetratedArmor / damageAfterStandardArmor;
                finalDamage *= Mth.clamp(compensationRatio, 1.0F, 3.0F);
            }
        }

        boolean isVanillaCrit = attacker.fallDistance > 0.0F
                && !attacker.onGround()
                && !attacker.onClimbable()
                && !attacker.isInWater()
                && !attacker.isPassenger();

        if (isVanillaCrit) {
            AttributeInstance critMultAttr = attacker.getAttribute(ModAttributes.CRIT_MULTIPLIER.get());
            float critMultiplier = critMultAttr != null ? (float) critMultAttr.getValue() : 1.5F;
            finalDamage = (finalDamage / 1.5F) * critMultiplier;

            ServerLevel serverLevel = attacker.serverLevel();
            PacketHandler.sendToNear(serverLevel, victim.position(), 36.0D,
                    new S2CSpawnRpgParticlesPacket(
                            S2CSpawnRpgParticlesPacket.Pattern.CRITICAL_BURST,
                            victim.getX(), victim.getY() + victim.getBbHeight() * 0.55D, victim.getZ(),
                            0.0D, 1.0D, 0.0D,
                            1.5F, 28
                    ));
        }

        event.setAmount(finalDamage);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerCombatData.get(serverPlayer).syncToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            PlayerCombatData oldData = PlayerCombatData.get(oldPlayer);
            PlayerCombatData newData = PlayerCombatData.get(newPlayer);
            newData.applySyncFromServer(
                    oldData.getMaxMana(),
                    oldData.getMaxMana(),
                    0,
                    oldData.getUltimateCooldown(),
                    0,
                    0
            );
            newData.saveToPlayerPersistentData(newPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerCombatData.get(serverPlayer).saveToPlayerPersistentData(serverPlayer);
            PlayerCombatData.removeServerCache(serverPlayer.getUUID());
        }
    }
}
