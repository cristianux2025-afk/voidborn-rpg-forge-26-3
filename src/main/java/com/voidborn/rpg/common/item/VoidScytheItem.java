package com.voidborn.rpg.common.item;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.client.render.VoidScytheRenderer;
import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.C2SSpecialAttackPacket;
import com.voidborn.rpg.network.packet.S2CCameraShakePacket;
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
import net.minecraft.world.entity.EquipmentSlot;
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

public class VoidScytheItem extends SwordItem implements GeoItem {
    public static final float WHIRLWIND_MANA_COST = 32.0F;
    public static final int WHIRLWIND_COOLDOWN_TICKS = 20 * 6;

    private static final RawAnimation IDLE_FLOAT = RawAnimation.begin().thenLoop("animation.void_scythe.idle_float");
    private static final RawAnimation SWEEP_COMBO_1 = RawAnimation.begin().thenPlay("animation.void_scythe.sweep_1");
    private static final RawAnimation SWEEP_COMBO_2 = RawAnimation.begin().thenPlay("animation.void_scythe.sweep_2");
    private static final RawAnimation WHIRLWIND_SPECIAL = RawAnimation.begin().thenPlay("animation.void_scythe.whirlwind_360");
    private static final RawAnimation ULTIMATE_REAP = RawAnimation.begin().thenPlay("animation.void_scythe.ultimate_reap");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VoidScytheItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(createScytheAttributes(tier)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private static ItemAttributeModifiers createScytheAttributes(Tier tier) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.5D + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.1D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.ARMOR_PENETRATION.get()),
                        new AttributeModifier(VoidbornRpgMod.id("scythe_armor_pen"), 0.38D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.MAX_MANA.get()),
                        new AttributeModifier(VoidbornRpgMod.id("scythe_soul_mana"), 30.0D, AttributeModifier.Operation.ADD_VALUE),
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

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer serverPlayer && !target.isAlive()) {
            PlayerCombatData data = PlayerCombatData.get(serverPlayer);
            data.applySyncFromServer(
                    Math.min(data.getMaxMana(), data.getCurrentMana() + 12.0F),
                    data.getMaxMana(),
                    data.getSpecialCooldown(),
                    data.getUltimateCooldown(),
                    data.getComboStage(),
                    data.getUltimateCastTicks()
            );
            data.syncToClient(serverPlayer);
            serverPlayer.heal(2.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    public void executeSweepComboServer(ServerPlayer player, ItemStack stack, PlayerCombatData data) {
        ServerLevel serverLevel = player.serverLevel();
        long itemId = GeoItem.getOrAssignId(stack, serverLevel);
        int stage = data.advanceCombo(2, 30);
        data.syncToClient(player);
        triggerAnim(player, itemId, "scythe_controller", "sweep_" + stage);
        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), "scythe_sweep_" + stage, 2, false));
    }

    public void executeWhirlwindSpecialServer(ServerPlayer player, ItemStack stack, PlayerCombatData data) {
        if (data.getSpecialCooldown() > 0 || !data.consumeMana(WHIRLWIND_MANA_COST)) {
            return;
        }

        ServerLevel serverLevel = player.serverLevel();
        long itemId = GeoItem.getOrAssignId(stack, serverLevel);

        data.setSpecialCooldown(WHIRLWIND_COOLDOWN_TICKS);
        data.syncToClient(player);
        player.getCooldowns().addCooldown(this, WHIRLWIND_COOLDOWN_TICKS);
        stack.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);

        triggerAnim(player, itemId, "scythe_controller", "whirlwind_360");
        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), "scythe_whirlwind_spin", 2, true));

        Vec3 center = player.position();
        double radius = 5.2D;

        serverLevel.playSound(null, center.x, center.y, center.z,
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2.0F, 0.65F);

        PacketHandler.sendToNear(serverLevel, center, 48.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.CLEAVE_ARC,
                        center.x, player.getEyeY() - 0.2D, center.z,
                        0.0D, 1.0D, 0.0D,
                        (float) radius, 48
                ));

        PacketHandler.sendToNear(serverLevel, center, 24.0D,
                new S2CCameraShakePacket(center, 22.0F, 0.45F, 10));

        AABB hitBox = player.getBoundingBox().inflate(radius, 2.0D, radius);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox,
                e -> e != player && e.isAlive() && !player.isAlliedTo(e));

        float attackDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.55F;
        float totalHeal = 0.0F;

        for (LivingEntity target : targets) {
            if (target.position().distanceTo(center) <= radius) {
                target.hurt(serverLevel.damageSources().playerAttack(player), attackDmg);
                Vec3 push = target.position().subtract(center).normalize().scale(0.85D);
                target.setDeltaMovement(push.x, 0.35D, push.z);
                target.hurtMarked = true;
                totalHeal += attackDmg * 0.15F;
            }
        }

        if (totalHeal > 0.0F) {
            player.heal(Math.min(10.0F, totalHeal));
        }
    }

    public void triggerUltimateWeaponAnimation(ServerPlayer player, ItemStack stack) {
        long itemId = GeoItem.getOrAssignId(stack, player.serverLevel());
        triggerAnim(player, itemId, "scythe_controller", "ultimate_reap");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "scythe_controller", 2, this::predicate)
                .triggerableAnim("sweep_1", SWEEP_COMBO_1)
                .triggerableAnim("sweep_2", SWEEP_COMBO_2)
                .triggerableAnim("whirlwind_360", WHIRLWIND_SPECIAL)
                .triggerableAnim("ultimate_reap", ULTIMATE_REAP));
    }

    private PlayState predicate(AnimationState<VoidScytheItem> state) {
        return state.setAndContinue(IDLE_FLOAT);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private VoidScytheRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new VoidScytheRenderer();
                }
                return this.renderer;
            }
        });
    }
}
