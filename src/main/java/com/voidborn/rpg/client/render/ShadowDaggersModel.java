package com.voidborn.rpg.client.render;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.item.ShadowDaggersItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ShadowDaggersModel extends GeoModel<ShadowDaggersItem> {
    private static final ResourceLocation MODEL = VoidbornRpgMod.id("geo/item/shadow_daggers.geo.json");
    private static final ResourceLocation TEXTURE = VoidbornRpgMod.id("textures/item/shadow_daggers.png");
    private static final ResourceLocation ANIMATION = VoidbornRpgMod.id("animations/item/shadow_daggers.animation.json");

    @Override
    public ResourceLocation getModelResource(ShadowDaggersItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ShadowDaggersItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ShadowDaggersItem animatable) {
        return ANIMATION;
    }
}
