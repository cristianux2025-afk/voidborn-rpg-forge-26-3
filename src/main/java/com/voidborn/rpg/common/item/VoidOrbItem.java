package com.voidborn.rpg.common.item;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoidOrbItem extends Item {
    public static final float SINGULARITY_MANA_COST = 35.0F;
    public static final int SINGULARITY_COOLDOWN_TICKS = 20 * 8;

    public VoidOrbItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerCombatData data = PlayerCombatData.get(serverPlayer);
            if (!player.getCooldowns().isOnCooldown(this) && data.consumeMana(SINGULARITY_MANA_COST)) {
                player.getCooldowns().addCooldown(this, SINGULARITY_COOLDOWN_TICKS);
                data.syncToClient(serverPlayer);

                ServerLevel serverLevel = serverPlayer.serverLevel();
                Vec3 vortexCenter = player.getEyePosition().add(player.getLookAngle().scale(5.5D));

                serverLevel.playSound(null, vortexCenter.x, vortexCenter.y, vortexCenter.z,
                        SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.8F, 0.6F);

                PacketHandler.sendToNear(serverLevel, vortexCenter, 48.0D,
                        new S2CSpawnRpgParticlesPacket(
                                S2CSpawnRpgParticlesPacket.Pattern.ULTIMATE_CHARGE_VORTEX,
                                vortexCenter.x, vortexCenter.y, vortexCenter.z,
                                0.0D, 1.0D, 0.0D, 4.5F, 55
                        ));

                AABB pullBox = new AABB(vortexCenter, vortexCenter).inflate(6.5D);
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, pullBox,
                        e -> e != player && e.isAlive() && !player.isAlliedTo(e)
                                && !(e instanceof TamableAnimal tamable && tamable.isOwnedBy(player)));

                for (LivingEntity target : targets) {
                    Vec3 pullVec = vortexCenter.subtract(target.position()).scale(0.38D);
                    target.setDeltaMovement(pullVec.x, Math.max(0.2D, pullVec.y), pullVec.z);
                    target.hurtMarked = true;
                    target.hurt(serverLevel.damageSources().indirectMagic(player, player), 8.0F);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
