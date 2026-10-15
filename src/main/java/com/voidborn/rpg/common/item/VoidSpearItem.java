package com.voidborn.rpg.common.item;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.C2SSpecialAttackPacket;
import com.voidborn.rpg.network.packet.S2CCameraShakePacket;
import com.voidborn.rpg.network.packet.S2CPlayerAnimationPacket;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import com.voidborn.rpg.registry.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class VoidSpearItem extends SwordItem implements GeoItem {
    public static final float PIERCE_MANA_COST = 24.0F;
    public static final int PIERCE_COOLDOWN_TICKS = 20 * 4 + 10;

    private static final RawAnimation IDLE_PULSE = RawAnimation.begin().thenLoop("animation.void_spear.idle");
    private static final RawAnimation COMBO_THRUST_1 = RawAnimation.begin().thenPlay("animation.void_spear.thrust_1");
    private static final RawAnimation COMBO_THRUST_2 = RawAnimation.begin().thenPlay("animation.void_spear.thrust_2");
    private static final RawAnimation SPECIAL_IMPALE = RawAnimation.begin().thenPlay("animation.void_spear.special_impale");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VoidSpearItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(createSpearAttributes(tier)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private static ItemAttributeModifiers createSpearAttributes(Tier tier) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.5D + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.1D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(VoidbornRpgMod.id("spear_reach"), 1.6D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.ARMOR_PENETRATION.get()),
                        new AttributeModifier(VoidbornRpgMod.id("spear_armor_pen"), 0.50D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.CRIT_MULTIPLIER.get()),
                        new AttributeModifier(VoidbornRpgMod.id("spear_crit_dmg"), 0.55D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            PacketHandler.sendToServer(new C2SSpecialAttackPacket(player.isShiftKeyDown()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public void executePiercingThrustSpecialServer(ServerPlayer player, ItemStack stack, PlayerCombatData data) {
        if (data.getSpecialCooldown() > 0 || !data.consumeMana(PIERCE_MANA_COST)) {
            return;
        }

        // v2.0: Desacoplamiento de desgaste de durabilidad previo al escaneo de entidades
        if (stack.getDamageValue() + 2 >= stack.getMaxDamage()) {
            stack.hurtAndBreak(2, player, ServerPlayer.getSlotForHand(InteractionHand.MAIN_HAND));
            return;
        }
        stack.hurtAndBreak(2, player, ServerPlayer.getSlotForHand(InteractionHand.MAIN_HAND));

        ServerLevel serverLevel = player.serverLevel();
        long itemId = GeoItem.getOrAssignId(stack, serverLevel);

        data.setSpecialCooldown(PIERCE_COOLDOWN_TICKS);
        data.syncToClient(player);
        player.getCooldowns().addCooldown(this, PIERCE_COOLDOWN_TICKS);

        triggerAnim(player, itemId, "spear_controller", "special_impale");
        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), "spear_abyssal_impale", 2, true));

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double maxDistance = 7.5D;
        Vec3 end = start.add(look.scale(maxDistance));

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.8F, 1.25F);

        PacketHandler.sendToNear(serverLevel, player.position(), 40.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.SHADOW_DASH_TRAIL,
                        start.x, start.y - 0.2D, start.z,
                        look.x, look.y, look.z,
                        (float) maxDistance, 50
                ));

        PacketHandler.sendToNear(serverLevel, player.position(), 24.0D,
                new S2CCameraShakePacket(player.position(), 20.0F, 0.45F, 10));

        AABB lineBox = new AABB(start, end).inflate(1.2D);
        float baseDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.75F;

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, lineBox,
                e -> e != player && e.isAlive() && !player.isAlliedTo(e));

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(player.position());
            double projection = toTarget.dot(look);
            if (projection > 0 && projection <= maxDistance) {
                target.hurt(serverLevel.damageSources().playerAttack(player), baseDmg);

                // v2.2: Purga de entidades muertas antes de aplicar vectores de succión
                if (target.isAlive() && !target.isRemoved() && !target.isDeadOrDying()) {
                    Vec3 linePoint = start.add(look.scale(projection));
                    Vec3 pullDiff = linePoint.subtract(target.position());
                    if (pullDiff.lengthSqr() > 0.001D) {
                        Vec3 pullVector = pullDiff.normalize().scale(0.65D);
                        target.setDeltaMovement(pullVector.x, 0.25D, pullVector.z);
                        target.hurtMarked = true;
                    }
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spear_controller", 1, this::predicate)
                .triggerableAnim("thrust_1", COMBO_THRUST_1)
                .triggerableAnim("thrust_2", COMBO_THRUST_2)
                .triggerableAnim("special_impale", SPECIAL_IMPALE));
    }

    private PlayState predicate(AnimationState<VoidSpearItem> state) {
        return state.setAndContinue(IDLE_PULSE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
