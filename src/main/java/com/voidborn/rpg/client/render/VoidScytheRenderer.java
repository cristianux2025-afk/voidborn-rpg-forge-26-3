package com.voidborn.rpg.client.render;

import com.voidborn.rpg.common.item.VoidScytheItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class VoidScytheRenderer extends GeoItemRenderer<VoidScytheItem> {
    public VoidScytheRenderer() {
        super(new VoidScytheModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
