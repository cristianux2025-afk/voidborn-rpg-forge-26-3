package com.voidborn.rpg.common.item;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoidCoreItem extends Item {
    public static final float SUMMON_MANA_COST = 45.0F;
    public static final int SUMMON_COOLDOWN_TICKS = 20 * 60;

    public VoidCoreItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerCombatData data = PlayerCombatData.get(serverPlayer);

            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }

            if (!data.consumeMana(SUMMON_MANA_COST)) {
                player.displayClientMessage(
                        Component.translatable("message.voidbornrpg.insufficient_mana", (int) SUMMON_MANA_COST)
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            player.getCooldowns().addCooldown(this, SUMMON_COOLDOWN_TICKS);
            data.syncToClient(serverPlayer);

            ServerLevel serverLevel = serverPlayer.serverLevel();
            Vec3 spawnPos = player.position().add(player.getLookAngle().x * 2.0D, 1.2D, player.getLookAngle().z * 2.0D);

            serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                    SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 2.0F, 0.75F);
            serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                    SoundEvents.WARDEN_EMERGE, SoundSource.PLAYERS, 1.5F, 1.2F);

            PacketHandler.sendToNear(serverLevel, spawnPos, 48.0D,
                    new S2CSpawnRpgParticlesPacket(
                            S2CSpawnRpgParticlesPacket.Pattern.ULTIMATE_CHARGE_VORTEX,
                            spawnPos.x, spawnPos.y, spawnPos.z,
                            0.0D, 1.0D, 0.0D, 3.8F, 60
                    ));

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 45, 1, false, false));

            player.displayClientMessage(
                    Component.translatable("message.voidbornrpg.sentinel_summoned").withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.void_core_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.void_core_stats")
                .withStyle(ChatFormatting.DARK_PURPLE));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
