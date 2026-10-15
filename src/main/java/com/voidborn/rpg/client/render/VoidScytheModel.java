package com.voidborn.rpg.client.render;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.common.item.VoidScytheItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidScytheModel extends GeoModel<VoidScytheItem> {
    private static final ResourceLocation MODEL = VoidbornRpgMod.id("geo/item/void_scythe.geo.json");
    private static final ResourceLocation TEXTURE = VoidbornRpgMod.id("textures/item/void_scythe.png");
    private static final ResourceLocation ANIMATION = VoidbornRpgMod.id("animations/item/void_scythe.animation.json");

    @Override
    public ResourceLocation getModelResource(VoidScytheItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(VoidScytheItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(VoidScytheItem animatable) {
        return ANIMATION;
    }
}
