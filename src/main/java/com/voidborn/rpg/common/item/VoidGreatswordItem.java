package com.voidborn.rpg.common.item;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.client.render.VoidGreatswordRenderer;
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

public class VoidGreatswordItem extends SwordItem implements GeoItem {
    public static final float SPECIAL_MANA_COST = 28.0F;
    public static final int SPECIAL_COOLDOWN_TICKS = 20 * 5;

    private static final RawAnimation IDLE_STANCE = RawAnimation.begin().thenLoop("animation.void_greatsword.idle_stance");
    private static final RawAnimation COMBO_SLASH_1 = RawAnimation.begin().thenPlay("animation.void_greatsword.combo_1");
    private static final RawAnimation COMBO_SLASH_2 = RawAnimation.begin().thenPlay("animation.void_greatsword.combo_2");
    private static final RawAnimation COMBO_SLASH_3 = RawAnimation.begin().thenPlay("animation.void_greatsword.combo_3");
    private static final RawAnimation SPECIAL_ABYSS_CLEAVE = RawAnimation.begin().thenPlay("animation.void_greatsword.special_cleave");
    private static final RawAnimation ULTIMATE_CATACLYSM = RawAnimation.begin().thenPlay("animation.void_greatsword.ultimate_cast");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VoidGreatswordItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(createRpgAttributes(tier)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private static ItemAttributeModifiers createRpgAttributes(Tier tier) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 11.0D + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.75D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MAX_HEALTH,
                        new AttributeModifier(VoidbornRpgMod.id("greatsword_vitality"), 6.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.ARMOR_PENETRATION.get()),
                        new AttributeModifier(VoidbornRpgMod.id("greatsword_armor_pen"), 0.30D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Holder.direct(ModAttributes.CRIT_MULTIPLIER.get()),
                        new AttributeModifier(VoidbornRpgMod.id("greatsword_crit_dmg"), 0.45D, AttributeModifier.Operation.ADD_VALUE),
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
        if (attacker instanceof ServerPlayer serverPlayer) {
            PlayerCombatData data = PlayerCombatData.get(serverPlayer);
            executeComboStepServer(serverPlayer, stack, data);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    public void executeComboStepServer(ServerPlayer player, ItemStack stack, PlayerCombatData data) {
        ServerLevel serverLevel = player.serverLevel();
        GeoItem.getOrAssignId(stack, serverLevel);

        int stage = data.advanceCombo(3, 35);
        data.syncToClient(player);

        String weaponAnimKey = "combo_" + stage;
        String playerAnimKey = "greatsword_combo_" + stage;

        triggerAnim(player, GeoItem.getId(stack), "combat_controller", weaponAnimKey);
        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), playerAnimKey, 2, false));

        if (stage == 3) {
            Vec3 look = player.getLookAngle();
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 0.7F);

            PacketHandler.sendToNear(serverLevel, player.position(), 32.0D,
                    new S2CSpawnRpgParticlesPacket(
                            S2CSpawnRpgParticlesPacket.Pattern.CLEAVE_ARC,
                            player.getX(), player.getEyeY() - 0.3D, player.getZ(),
                            look.x, look.y, look.z,
                            3.2F, 20
                    ));
        }
    }

    public void executeSpecialAttackServer(ServerPlayer player, ItemStack stack, PlayerCombatData data) {
        if (data.getSpecialCooldown() > 0 || !data.consumeMana(SPECIAL_MANA_COST)) {
            return;
        }

        ServerLevel serverLevel = player.serverLevel();
        long itemId = GeoItem.getOrAssignId(stack, serverLevel);

        data.setSpecialCooldown(SPECIAL_COOLDOWN_TICKS);
        data.syncToClient(player);
        player.getCooldowns().addCooldown(this, SPECIAL_COOLDOWN_TICKS);

        triggerAnim(player, itemId, "combat_controller", "special_cleave");
        PacketHandler.sendToTrackingAndSelf(player,
                new S2CPlayerAnimationPacket(player.getId(), "greatsword_abyss_cleave", 3, true));

        Vec3 look = player.getLookAngle();
        Vec3 origin = player.position();

        serverLevel.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 1.8F, 0.8F);

        PacketHandler.sendToNear(serverLevel, origin, 48.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.CLEAVE_ARC,
                        origin.x, player.getEyeY() - 0.25D, origin.z,
                        look.x, look.y, look.z,
                        4.8F, 42
                ));

        PacketHandler.sendToNear(serverLevel, origin, 24.0D,
                new S2CCameraShakePacket(origin, 20.0F, 0.55F, 12));

        double range = 4.8D;
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float cleaveDamage = baseDamage * 1.65F;

        AABB searchBox = player.getBoundingBox().inflate(range, 2.5D, range);
        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox,
                target -> target != player && target.isAlive() && !player.isAlliedTo(target));

        for (LivingEntity target : entities) {
            Vec3 toTarget = target.position().subtract(origin);
            double distance = toTarget.length();
            if (distance <= range) {
                double dot = toTarget.normalize().dot(new Vec3(look.x, 0.0D, look.z).normalize());
                if (dot >= -0.15D) {
                    target.hurt(serverLevel.damageSources().playerAttack(player), cleaveDamage);

                    Vec3 pushDir = toTarget.normalize();
                    target.setDeltaMovement(pushDir.x * 1.35D, 0.48D, pushDir.z * 1.35D);
                    target.hurtMarked = true;
                }
            }
        }
    }

    public void triggerUltimateWeaponAnimation(ServerPlayer player, ItemStack stack) {
        long itemId = GeoItem.getOrAssignId(stack, player.serverLevel());
        triggerAnim(player, itemId, "combat_controller", "ultimate_cast");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "combat_controller", 2, this::predicate)
                .triggerableAnim("combo_1", COMBO_SLASH_1)
                .triggerableAnim("combo_2", COMBO_SLASH_2)
                .triggerableAnim("combo_3", COMBO_SLASH_3)
                .triggerableAnim("special_cleave", SPECIAL_ABYSS_CLEAVE)
                .triggerableAnim("ultimate_cast", ULTIMATE_CATACLYSM));
    }

    private PlayState predicate(AnimationState<VoidGreatswordItem> state) {
        return state.setAndContinue(IDLE_STANCE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private VoidGreatswordRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new VoidGreatswordRenderer();
                }
                return this.renderer;
            }
        });
    }
}
