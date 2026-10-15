package com.voidborn.rpg.client.anim;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.common.item.ShadowDaggersItem;
import com.voidborn.rpg.common.item.VoidGreatswordItem;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerAnimationHandler {
    public static final ResourceLocation ANIMATION_LAYER_ID = VoidbornRpgMod.id("combat_animation_layer");
    private static final Map<UUID, ProceduralBonePose> FALLBACK_POSES = new ConcurrentHashMap<>();

    public static void registerPlayerAnimatorFactory() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ANIMATION_LAYER_ID,
                42,
                player -> new ModifierLayer<>()
        );
    }

    @SuppressWarnings("unchecked")
    public static void playAnimationOnClientPlayer(
            AbstractClientPlayer player,
            String animationName,
            int fadeTicks,
            boolean showRightArmFirstPerson
    ) {
        var associatedData = PlayerAnimationAccess.getPlayerAssociatedData(player);
        var layerObj = associatedData.get(ANIMATION_LAYER_ID);

        if (layerObj instanceof ModifierLayer<?> rawLayer) {
            ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>) rawLayer;
            KeyframeAnimation animData = PlayerAnimationRegistry.getAnimation(VoidbornRpgMod.id(animationName));

            if (animData != null) {
                KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(animData)
                        .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                        .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                .setShowRightArm(showRightArmFirstPerson)
                                .setShowLeftArm(showRightArmFirstPerson)
                                .setShowRightItem(true)
                                .setShowLeftItem(true));

                animationLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(fadeTicks, Ease.INOUTSINE),
                        animPlayer
                );
                return;
            }
        }

        FALLBACK_POSES.put(player.getUUID(), new ProceduralBonePose(animationName, player.tickCount, 18));
    }

    public static void applyProceduralCombatStance(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        float partialTick = event.getPartialTick();
        float ageInTicks = player.tickCount + partialTick;

        if (mainHand.getItem() instanceof VoidGreatswordItem && !player.swinging) {
            model.rightArm.xRot = -1.15F + Mth.sin(ageInTicks * 0.08F) * 0.04F;
            model.rightArm.yRot = -0.35F;
            model.rightArm.zRot = 0.18F;

            model.leftArm.xRot = -0.95F + Mth.sin(ageInTicks * 0.08F) * 0.04F;
            model.leftArm.yRot = 0.55F;
        } else if (mainHand.getItem() instanceof ShadowDaggersItem && !player.swinging) {
            model.rightArm.xRot = -0.65F;
            model.rightArm.yRot = 0.25F;
            model.leftArm.xRot = -0.75F;
            model.leftArm.yRot = -0.30F;
        }

        PlayerCombatData combatData = PlayerCombatData.get(player);
        if (combatData.isCastingUltimate()) {
            float castProgress = 1.0F - (combatData.getUltimateCastTicks() / 25.0F);
            if (castProgress < 0.72F) {
                float lift = Mth.clamp(castProgress / 0.72F, 0.0F, 1.0F);
                model.rightArm.xRot = Mth.lerp(lift, -0.8F, -2.75F);
                model.rightArm.zRot = Mth.lerp(lift, 0.1F, 0.45F);
                model.leftArm.xRot = Mth.lerp(lift, -0.8F, -2.75F);
                model.leftArm.zRot = Mth.lerp(lift, -0.1F, -0.45F);
                model.head.xRot = Mth.lerp(lift, 0.0F, -0.55F);
            } else {
                float slam = (castProgress - 0.72F) / 0.28F;
                model.rightArm.xRot = Mth.lerp(slam, -2.75F, 0.45F);
                model.leftArm.xRot = Mth.lerp(slam, -2.75F, 0.45F);
                model.body.xRot = Mth.lerp(slam, -0.15F, 0.42F);
            }
        }

        ProceduralBonePose fallback = FALLBACK_POSES.get(player.getUUID());
        if (fallback != null) {
            float elapsed = (player.tickCount - fallback.startTick) + partialTick;
            if (elapsed > fallback.durationTicks) {
                FALLBACK_POSES.remove(player.getUUID());
            } else {
                float t = elapsed / (float) fallback.durationTicks;
                float swingArc = Mth.sin(t * Mth.PI);
                model.body.yRot += swingArc * 0.65F;
                model.rightArm.xRot = -1.6F + swingArc * 2.1F;
                model.rightArm.zRot = 0.65F;
            }
        }
    }

    private record ProceduralBonePose(String name, int startTick, int durationTicks) {}
}
