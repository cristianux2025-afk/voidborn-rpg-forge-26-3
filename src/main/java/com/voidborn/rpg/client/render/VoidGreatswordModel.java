package com.voidborn.rpg.client.render;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.item.VoidGreatswordItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidGreatswordModel extends GeoModel<VoidGreatswordItem> {
    private static final ResourceLocation MODEL = VoidbornRpgMod.id("geo/item/void_greatsword.geo.json");
    private static final ResourceLocation TEXTURE = VoidbornRpgMod.id("textures/item/void_greatsword.png");
    private static final ResourceLocation ANIMATION = VoidbornRpgMod.id("animations/item/void_greatsword.animation.json");

    @Override
    public ResourceLocation getModelResource(VoidGreatswordItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(VoidGreatswordItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(VoidGreatswordItem animatable) {
        return ANIMATION;
    }
}
