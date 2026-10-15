package com.voidborn.rpg.network.packet;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.common.item.ShadowDaggersItem;
import com.voidborn.rpg.common.item.VoidGreatswordItem;
import com.voidborn.rpg.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class C2SUltimateSkillPacket {
    public static final float ULTIMATE_MANA_COST = 65.0F;
    public static final int ULTIMATE_COOLDOWN_TICKS = 20 * 18;
    public static final int ULTIMATE_CAST_DURATION_TICKS = 25;

    public C2SUltimateSkillPacket() {}

    public static void encode(C2SUltimateSkillPacket msg, FriendlyByteBuf buf) {}

    public static C2SUltimateSkillPacket decode(FriendlyByteBuf buf) {
        return new C2SUltimateSkillPacket();
    }

    public static void handle(C2SUltimateSkillPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean hasValidRpgWeapon = mainHand.getItem() instanceof VoidGreatswordItem
                || mainHand.getItem() instanceof ShadowDaggersItem;

        if (!hasValidRpgWeapon) {
            player.displayClientMessage(
                    Component.translatable("message.voidbornrpg.require_weapon").withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        PlayerCombatData data = PlayerCombatData.get(player);

        if (data.getUltimateCooldown() > 0) {
            float secondsLeft = data.getUltimateCooldown() / 20.0F;
            player.displayClientMessage(
                    Component.translatable("message.voidbornrpg.ultimate_on_cooldown", String.format("%.1f", secondsLeft))
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
            return;
        }

        if (!data.consumeMana(ULTIMATE_MANA_COST)) {
            player.displayClientMessage(
                    Component.translatable("message.voidbornrpg.insufficient_mana", (int) ULTIMATE_MANA_COST)
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            return;
        }

        data.setUltimateCooldown(ULTIMATE_COOLDOWN_TICKS);
        data.setUltimateCastTicks(ULTIMATE_CAST_DURATION_TICKS);
        data.syncToClient(player);

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ULTIMATE_CAST_DURATION_TICKS + 10, 3, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ULTIMATE_CAST_DURATION_TICKS, 4, false, false));

        player.setDeltaMovement(player.getDeltaMovement().x * 0.2D, 0.52D, player.getDeltaMovement().z * 0.2D);
        player.hurtMarked = true;

        if (mainHand.getItem() instanceof VoidGreatswordItem greatsword) {
            greatsword.triggerUltimateWeaponAnimation(player, mainHand);
        } else if (mainHand.getItem() instanceof ShadowDaggersItem daggers) {
            daggers.triggerUltimateWeaponAnimation(player, mainHand);
        }

        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), "void_cataclysm_ultimate", 4, true));

        ServerLevel serverLevel = player.serverLevel();
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 2.0F, 0.85F);

        PacketHandler.sendToNear(serverLevel, player.position(), 48.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.ULTIMATE_CHARGE_VORTEX,
                        player.getX(), player.getY() + 1.0D, player.getZ(),
                        player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z,
                        4.0F, 65
                ));

        PacketHandler.sendToNear(serverLevel, player.position(), 32.0D,
                new S2CCameraShakePacket(player.position(), 24.0F, 0.35F, 18));
    }
}
