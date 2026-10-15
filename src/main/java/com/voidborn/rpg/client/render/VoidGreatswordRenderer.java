package com.voidborn.rpg.client.render;

import com.voidborn.rpg.common.item.VoidGreatswordItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class VoidGreatswordRenderer extends GeoItemRenderer<VoidGreatswordItem> {
    public VoidGreatswordRenderer() {
        super(new VoidGreatswordModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
