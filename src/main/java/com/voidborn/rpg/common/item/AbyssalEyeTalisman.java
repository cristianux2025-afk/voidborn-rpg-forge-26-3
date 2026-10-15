package com.voidborn.rpg.common.item;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CCameraShakePacket;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AbyssalEyeTalisman extends Item {
    public static final int EMERGENCY_COOLDOWN_TICKS = 20 * 90;

    public AbyssalEyeTalisman() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public static boolean triggerEmergencyDefiance(ServerPlayer player, ItemStack talismanStack) {
        if (player.getCooldowns().isOnCooldown(talismanStack.getItem())) {
            return false;
        }

        float healthRatio = player.getHealth() / player.getMaxHealth();
        if (healthRatio > 0.25F) {
            return false;
        }

        player.getCooldowns().addCooldown(talismanStack.getItem(), EMERGENCY_COOLDOWN_TICKS);
        ServerLevel serverLevel = player.serverLevel();
        Vec3 pos = player.position();

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 2, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));

        PlayerCombatData combatData = PlayerCombatData.get(player);
        combatData.applySyncFromServer(
                Math.min(combatData.getMaxMana(), combatData.getCurrentMana() + 50.0F),
                combatData.getMaxMana(),
                combatData.getSpecialCooldown(),
                combatData.getUltimateCooldown(),
                combatData.getComboStage(),
                combatData.getUltimateCastTicks()
        );
        combatData.syncToClient(player);

        serverLevel.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 0.5F);
        serverLevel.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.6F, 1.4F);

        AABB blastArea = player.getBoundingBox().inflate(6.0D, 3.0D, 6.0D);
        List<LivingEntity> enemies = serverLevel.getEntitiesOfClass(LivingEntity.class, blastArea,
                e -> e != player && e.isAlive() && !player.isAlliedTo(e));

        for (LivingEntity enemy : enemies) {
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 4));
            enemy.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            Vec3 knockback = enemy.position().subtract(pos).normalize().scale(1.2D);
            enemy.setDeltaMovement(knockback.x, 0.4D, knockback.z);
            enemy.hurtMarked = true;
        }

        PacketHandler.sendToNear(serverLevel, pos, 48.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.ULTIMATE_DETONATION_SHOCKWAVE,
                        pos.x, player.getEyeY() - 0.2D, pos.z,
                        0.0D, 1.0D, 0.0D, 6.0F, 75
                ));

        PacketHandler.sendToNear(serverLevel, pos, 28.0D,
                new S2CCameraShakePacket(pos, 24.0F, 0.65F, 16));

        player.displayClientMessage(
                Component.translatable("message.voidbornrpg.talisman_activated").withStyle(ChatFormatting.DARK_PURPLE),
                true
        );

        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.abyssal_eye_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.abyssal_eye_effect")
                .withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
