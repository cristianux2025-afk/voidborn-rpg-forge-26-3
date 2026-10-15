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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoidElixirItem extends Item {
    public static final int DRINK_DURATION = 32;
    public static final int ELIXIR_COOLDOWN_TICKS = 20 * 60;

    public VoidElixirItem() {
        super(new Item.Properties().stacksTo(4).rarity(Rarity.RARE).fireResistant());
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            ServerLevel serverLevel = player.serverLevel();
            Vec3 pos = player.position();

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 25, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 25, 1, false, true));
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.POISON);

            PlayerCombatData data = PlayerCombatData.get(player);
            data.applySyncFromServer(
                    Math.min(data.getMaxMana(), data.getCurrentMana() + 60.0F),
                    data.getMaxMana(),
                    data.getSpecialCooldown(),
                    data.getUltimateCooldown(),
                    data.getComboStage(),
                    data.getUltimateCastTicks()
            );
            data.syncToClient(player);

            player.getCooldowns().addCooldown(this, ELIXIR_COOLDOWN_TICKS);

            serverLevel.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.8F, 0.7F);
            serverLevel.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 0.85F);

            PacketHandler.sendToNear(serverLevel, pos, 36.0D,
                    new S2CSpawnRpgParticlesPacket(
                            S2CSpawnRpgParticlesPacket.Pattern.CRITICAL_BURST,
                            pos.x, player.getEyeY(), pos.z,
                            0.0D, 1.0D, 0.0D, 2.5F, 40
                    ));

            player.displayClientMessage(
                    Component.translatable("message.voidbornrpg.elixir_consumed").withStyle(ChatFormatting.DARK_PURPLE),
                    true
            );
        }

        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.void_elixir_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.void_elixir_effects")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
