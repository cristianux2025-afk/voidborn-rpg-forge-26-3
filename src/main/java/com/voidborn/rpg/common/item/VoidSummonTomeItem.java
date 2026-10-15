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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoidSummonTomeItem extends Item {
    public static final float SUMMON_MANA_COST = 45.0F;
    public static final int SUMMON_COOLDOWN_TICKS = 20 * 30;
    public static final int SPECTER_LIFETIME_TICKS = 20 * 35;

    public VoidSummonTomeItem() {
        super(new Item.Properties().stacksTo(1).durability(450).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerCombatData data = PlayerCombatData.get(serverPlayer);

            if (data.getSpecialCooldown() > 0) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.voidbornrpg.tome_on_cooldown").withStyle(ChatFormatting.GOLD),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            if (!data.consumeMana(SUMMON_MANA_COST)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.voidbornrpg.insufficient_mana", (int) SUMMON_MANA_COST)
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            ServerLevel serverLevel = serverPlayer.serverLevel();
            Vec3 spawnPos = serverPlayer.position().add(serverPlayer.getLookAngle().scale(2.0D)).add(0, 1.2D, 0);

            Vex specter = EntityType.VEX.create(serverLevel);
            if (specter != null) {
                specter.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, serverPlayer.getYRot(), 0.0F);
                specter.setOwner(serverPlayer);
                specter.setLimitedLife(SPECTER_LIFETIME_TICKS);
                specter.setCustomName(Component.literal("Espectro del Vacío").withStyle(ChatFormatting.DARK_PURPLE));
                specter.setCustomNameVisible(true);
                serverLevel.addFreshEntity(specter);
            }

            data.setSpecialCooldown(SUMMON_COOLDOWN_TICKS);
            data.syncToClient(serverPlayer);
            serverPlayer.getCooldowns().addCooldown(this, SUMMON_COOLDOWN_TICKS);

            stack.hurtAndBreak(1, serverPlayer, ServerPlayer.getSlotForHand(hand));

            serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                    SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 1.8F, 0.75F);
            serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.2F, 1.4F);

            PacketHandler.sendToNear(serverLevel, spawnPos, 40.0D,
                    new S2CSpawnRpgParticlesPacket(
                            S2CSpawnRpgParticlesPacket.Pattern.ULTIMATE_CHARGE_VORTEX,
                            spawnPos.x, spawnPos.y, spawnPos.z,
                            0.0D, 1.0D, 0.0D, 3.2F, 45
                    ));

            PacketHandler.sendToNear(serverLevel, spawnPos, 20.0D,
                    new S2CCameraShakePacket(spawnPos, 16.0F, 0.35F, 10));

            serverPlayer.displayClientMessage(
                    Component.translatable("message.voidbornrpg.specter_summoned").withStyle(ChatFormatting.DARK_PURPLE),
                    true
            );

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.summon_tome_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("• Invoca: Espectro Guardián del Vacío (35s de duración)")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.literal("• Costo: 45 Maná | Enfriamiento: 30s")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
