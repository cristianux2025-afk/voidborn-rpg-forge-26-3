package com.voidborn.rpg.client.render;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.item.VoidbornArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidbornArmorModel extends GeoModel<VoidbornArmorItem> {
    private static final ResourceLocation MODEL = VoidbornRpgMod.id("geo/armor/voidborn_armor.geo.json");
    private static final ResourceLocation TEXTURE = VoidbornRpgMod.id("textures/armor/voidborn_armor.png");
    private static final ResourceLocation ANIMATION = VoidbornRpgMod.id("animations/armor/voidborn_armor.animation.json");

    @Override
    public ResourceLocation getModelResource(VoidbornArmorItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(VoidbornArmorItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(VoidbornArmorItem animatable) {
        return ANIMATION;
    }
}
