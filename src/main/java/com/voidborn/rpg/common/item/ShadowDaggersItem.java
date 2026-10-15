package com.voidborn.rpg.common.item;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.client.render.ShadowDaggersRenderer;
import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.C2SSpecialAttackPacket;
import com.voidborn.rpg.network.packet.S2CPlayerAnimationPacket;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import com.voidborn.rpg.registry.ModAttributes;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
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
import java.util.function.Consumer;

public class ShadowDaggersItem extends SwordItem implements GeoItem {
    public static final float SHADOW_STEP_MANA_COST = 22.0F;
    public static final int SHADOW_STEP_COOLDOWN_TICKS = 20 * 4;

    private static final RawAnimation IDLE_SPIN = RawAnimation.begin().thenLoop("animation.shadow_daggers.idle");
    private static final RawAnimation FLURRY_1 = RawAnimation.begin().thenPlay("animation.shadow_daggers.flurry_1");
    private static final RawAnimation FLURRY_2 = RawAnimation.begin().thenPlay("animation.shadow_daggers.flurry_2");
    private static final RawAnimation SHADOW_DASH = RawAnimation.begin().thenPlay("animation.shadow_daggers.shadow_dash");
    private static final RawAnimation ULTIMATE_DANCE = RawAnimation.begin().thenPlay("animation.shadow_daggers.ultimate_dance");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShadowDaggersItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(createDaggerAttributes(tier)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private static ItemAttributeModifiers createDaggerAttributes(Tier tier) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.5D + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -1.4D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(VoidbornRpgMod.id("dagger_agility"), 0.12D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.ARMOR_PENETRATION.get()),
                        new AttributeModifier(VoidbornRpgMod.id("dagger_armor_pen"), 0.45D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.CRIT_MULTIPLIER.get()),
                        new AttributeModifier(VoidbornRpgMod.id("dagger_crit_dmg"), 0.75D, AttributeModifier.Operation.ADD_VALUE),
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

    public void executeRapidComboServer(ServerPlayer player, ItemStack stack, PlayerCombatData data) {
        ServerLevel serverLevel = player.serverLevel();
        long itemId = GeoItem.getOrAssignId(stack, serverLevel);
        int stage = data.advanceCombo(2, 25);
        data.syncToClient(player);

        triggerAnim(player, itemId, "dagger_controller", "flurry_" + stage);
        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), "dagger_flurry_" + stage, 1, false));
    }

    public void executeShadowStepSpecialServer(ServerPlayer player, ItemStack stack, PlayerCombatData data) {
        if (data.getSpecialCooldown() > 0 || !data.consumeMana(SHADOW_STEP_MANA_COST)) {
            return;
        }

        ServerLevel serverLevel = player.serverLevel();
        long itemId = GeoItem.getOrAssignId(stack, serverLevel);

        data.setSpecialCooldown(SHADOW_STEP_COOLDOWN_TICKS);
        data.syncToClient(player);
        player.getCooldowns().addCooldown(this, SHADOW_STEP_COOLDOWN_TICKS);

        triggerAnim(player, itemId, "dagger_controller", "shadow_dash");
        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), "shadow_step_dash", 2, true));

        Vec3 startPos = player.position();
        Vec3 look = player.getLookAngle().normalize();
        double dashDistance = 6.5D;

        player.setDeltaMovement(look.x * 1.95D, 0.22D, look.z * 1.95D);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 1, false, false));

        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.4F, 1.35F);

        PacketHandler.sendToNear(serverLevel, startPos, 40.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.SHADOW_DASH_TRAIL,
                        startPos.x, startPos.y + 1.0D, startPos.z,
                        look.x, look.y, look.z,
                        (float) dashDistance, 45
                ));

        AABB dashBox = player.getBoundingBox().expandTowards(look.scale(dashDistance)).inflate(1.5D);
        float strikeDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.85F;

        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, dashBox,
                e -> e != player && e.isAlive() && !player.isAlliedTo(e));

        for (LivingEntity victim : victims) {
            victim.hurt(serverLevel.damageSources().playerAttack(player), strikeDamage);
            victim.setDeltaMovement(0.0D, 0.55D, 0.0D);
            victim.hurtMarked = true;
        }
    }

    public void triggerUltimateWeaponAnimation(ServerPlayer player, ItemStack stack) {
        long itemId = GeoItem.getOrAssignId(stack, player.serverLevel());
        triggerAnim(player, itemId, "dagger_controller", "ultimate_dance");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "dagger_controller", 1, this::predicate)
                .triggerableAnim("flurry_1", FLURRY_1)
                .triggerableAnim("flurry_2", FLURRY_2)
                .triggerableAnim("shadow_dash", SHADOW_DASH)
                .triggerableAnim("ultimate_dance", ULTIMATE_DANCE));
    }

    private PlayState predicate(AnimationState<ShadowDaggersItem> state) {
        return state.setAndContinue(IDLE_SPIN);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ShadowDaggersRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ShadowDaggersRenderer();
                }
                return this.renderer;
            }
        });
    }
}
