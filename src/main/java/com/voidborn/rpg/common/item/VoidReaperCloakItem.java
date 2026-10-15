package com.voidborn.rpg.common.item;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.network.PacketHandler;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import com.voidborn.rpg.registry.ModArmorMaterials;
import com.voidborn.rpg.registry.ModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class VoidReaperCloakItem extends ArmorItem implements GeoItem {
    public static final float VEIL_MANA_COST = 30.0F;
    public static final int VEIL_COOLDOWN_TICKS = 20 * 20;

    private static final RawAnimation CLOAK_IDLE = RawAnimation.begin().thenLoop("animation.void_cloak.idle_flow");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VoidReaperCloakItem() {
        super(ModArmorMaterials.VOIDBORN, Type.CHESTPLATE,
                new Properties().stacksTo(1).durability(620).rarity(Rarity.EPIC).fireResistant()
                        .attributes(createCloakModifiers()));
    }

    private static ItemAttributeModifiers createCloakModifiers() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(VoidbornRpgMod.id("cloak_armor"), 7.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.CHEST)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(VoidbornRpgMod.id("cloak_toughness"), 3.5D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.CHEST)
                .add(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(VoidbornRpgMod.id("cloak_speed"), 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        EquipmentSlotGroup.CHEST)
                .add(Holder.direct(ModAttributes.MAX_MANA.get()),
                        new AttributeModifier(VoidbornRpgMod.id("cloak_mana"), 35.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.CHEST)
                .build();
    }

    public static void activateVoidVeil(ServerPlayer player, ItemStack cloak) {
        PlayerCombatData data = PlayerCombatData.get(player);

        if (data.getSpecialCooldown() > 0 || !data.consumeMana(VEIL_MANA_COST)) {
            return;
        }

        data.setSpecialCooldown(VEIL_COOLDOWN_TICKS);
        data.syncToClient(player);
        player.getCooldowns().addCooldown(cloak.getItem(), VEIL_COOLDOWN_TICKS);

        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 4, false, false));

        ServerLevel serverLevel = player.serverLevel();
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.6F, 1.4F);

        PacketHandler.sendToNear(serverLevel, player.position(), 36.0D,
                new S2CSpawnRpgParticlesPacket(
                        S2CSpawnRpgParticlesPacket.Pattern.SHADOW_DASH_TRAIL,
                        player.getX(), player.getY() + 1.0D, player.getZ(),
                        0.0D, 1.0D, 0.0D, 2.8F, 35
                ));

        player.displayClientMessage(
                Component.translatable("message.voidbornrpg.veil_activated").withStyle(ChatFormatting.DARK_PURPLE),
                true
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "cloak_controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<VoidReaperCloakItem> state) {
        return state.setAndContinue(CLOAK_IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.voidbornrpg.cloak_desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("• Habilidad: Velo del Vacío (Invisibilidad + Inmunidad por 4s)")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.literal("• Pasiva: +10% Velocidad de Movimiento")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
