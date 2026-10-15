package com.voidborn.rpg.common.item;

import com.google.common.base.Suppliers;
import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.client.render.VoidbornArmorRenderer;
import com.voidborn.rpg.registry.ModAttributes;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VoidbornArmorItem extends ArmorItem implements GeoItem {
    private static final RawAnimation ARMOR_IDLE = RawAnimation.begin().thenLoop("animation.voidborn_armor.idle_pulse");
    private static final RawAnimation ARMOR_COMBAT_MANTLE = RawAnimation.begin().thenLoop("animation.voidborn_armor.combat_flare");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<ItemAttributeModifiers> rpgModifiers;

    public VoidbornArmorItem(
            Holder<ArmorMaterial> material,
            Type type,
            Properties properties,
            double bonusHealth,
            double bonusArmorPenetration,
            double bonusMaxMana,
            double bonusAttackDamage
    ) {
        super(material, type, properties);
        this.rpgModifiers = Suppliers.memoize(() -> {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
            ResourceLocation baseId = VoidbornRpgMod.id("armor_" + type.getName());

            int defense = material.value().getDefense(type);
            float toughness = material.value().toughness();
            float kbResistance = material.value().knockbackResistance();

            builder.add(Attributes.ARMOR,
                    new AttributeModifier(baseId.withSuffix("_defense"), defense, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup);
            builder.add(Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(baseId.withSuffix("_toughness"), toughness, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup);
            if (kbResistance > 0.0F) {
                builder.add(Attributes.KNOCKBACK_RESISTANCE,
                        new AttributeModifier(baseId.withSuffix("_kb_res"), kbResistance, AttributeModifier.Operation.ADD_VALUE),
                        slotGroup);
            }

            builder.add(Attributes.MAX_HEALTH,
                    new AttributeModifier(baseId.withSuffix("_health"), bonusHealth, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup);
            builder.add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(baseId.withSuffix("_attack"), bonusAttackDamage, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup);
            builder.add(Holder.direct(ModAttributes.ARMOR_PENETRATION.get()),
                    new AttributeModifier(baseId.withSuffix("_armor_pen"), bonusArmorPenetration, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup);
            builder.add(Holder.direct(ModAttributes.MAX_MANA.get()),
                    new AttributeModifier(baseId.withSuffix("_mana"), bonusMaxMana, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup);

            return builder.build();
        });
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return this.rpgModifiers.get();
    }

    public static boolean hasFullVoidbornSet(LivingEntity entity) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof VoidbornArmorItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "armor_controller", 10, this::predicate));
    }

    private PlayState predicate(AnimationState<VoidbornArmorItem> state) {
        if (state.getData(DataTickets.ENTITY) instanceof Player player) {
            if (player.isSprinting() || player.swinging) {
                return state.setAndContinue(ARMOR_COMBAT_MANTLE);
            }
        }
        return state.setAndContinue(ARMOR_IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private VoidbornArmorRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity livingEntity,
                    ItemStack itemStack,
                    EquipmentSlot equipmentSlot,
                    HumanoidModel<?> original
            ) {
                if (this.renderer == null) {
                    this.renderer = new VoidbornArmorRenderer();
                }
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }
}
