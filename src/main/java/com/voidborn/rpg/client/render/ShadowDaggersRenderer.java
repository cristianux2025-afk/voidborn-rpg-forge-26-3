package com.voidborn.rpg.client.render;

import com.voidborn.rpg.common.item.ShadowDaggersItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ShadowDaggersRenderer extends GeoItemRenderer<ShadowDaggersItem> {
    public ShadowDaggersRenderer() {
        super(new ShadowDaggersModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
