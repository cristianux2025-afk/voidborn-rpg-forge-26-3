package com.voidborn.rpg.common.item;

import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CCameraShakePacket;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class VoidRingItem extends Item {
    public VoidRingItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public static boolean hasRing(Player player) {
        if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof VoidRingItem) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof VoidRingItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * v2.1: Resolución segura por UUID y clave de dimensión en tarea diferida de 12 ticks.
     */
    public static void triggerAbyssalEcho(ServerPlayer player, Vec3 origin, double radius, float baseDamage) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        UUID playerUuid = player.getUUID();
        ResourceKey<Level> dimensionKey = player.level().dimension();
        float echoDamage = baseDamage * 0.35F;

        server.tell(new net.minecraft.server.TickTask(server.getTickCount() + 12, () -> {
            ServerPlayer activePlayer = server.getPlayerList().getPlayer(playerUuid);
            ServerLevel targetLevel = server.getLevel(dimensionKey);
            if (activePlayer == null || !activePlayer.isAlive() || targetLevel == null || activePlayer.level() != targetLevel) {
                return;
            }

            targetLevel.playSound(null, origin.x, origin.y, origin.z,
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.6F, 1.4F);

            PacketHandler.sendToNear(targetLevel, origin, 36.0D,
                    new S2CSpawnRpgParticlesPacket(
                            S2CSpawnRpgParticlesPacket.Pattern.SHADOW_DASH_TRAIL,
                            origin.x, origin.y + 0.5D, origin.z,
                            0.0D, 1.0D, 0.0D, (float) radius, 40
                    ));

            PacketHandler.sendToNear(targetLevel, origin, 20.0D,
                    new S2CCameraShakePacket(origin, 18.0F, 0.30F, 8));

            AABB hitBox = new AABB(origin.x - radius, origin.y - 2.0D, origin.z - radius,
                    origin.x + radius, origin.y + 2.0D, origin.z + radius);

            List<LivingEntity> targets = targetLevel.getEntitiesOfClass(LivingEntity.class, hitBox,
                    e -> e != activePlayer && e.isAlive() && !activePlayer.isAlliedTo(e));

            for (LivingEntity target : targets) {
                target.hurt(targetLevel.damageSources().playerAttack(activePlayer), echoDamage);
                if (target.isAlive() && !target.isRemoved()) {
                    target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.28D, 0.0D));
                    target.hurtMarked = true;
                }
            }
        }));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            if (player.getY() < 0 && player.tickCount % 60 == 0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.NIGHT_VISION, 200, 0, false, false));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.ring_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("• Pasiva: Eco del Abismo (35% de daño residual diferido)")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.literal("• Pasiva: Visión Abisal bajo Y=0")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
