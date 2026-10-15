package com.voidborn.rpg.client.render;

import com.voidborn.rpg.common.item.VoidbornArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class VoidbornArmorRenderer extends GeoArmorRenderer<VoidbornArmorItem> {
    public VoidbornArmorRenderer() {
        super(new VoidbornArmorModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
