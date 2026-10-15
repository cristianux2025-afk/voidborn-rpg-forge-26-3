package com.voidborn.rpg.client;

import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;

public class CameraShakeHandler {
    private static float shakeIntensity = 0.0F;
    private static int remainingTicks = 0;
    private static int totalDuration = 1;

    public static void triggerShake(float intensity, int durationTicks) {
        // v2.0: Acumulación amortiguada de trauma con techo de seguridad
        float residual = remainingTicks > 0 ? shakeIntensity * (remainingTicks / (float) totalDuration) : 0.0F;
        shakeIntensity = Math.min(1.5F, Math.max(residual, intensity));
        remainingTicks = Math.max(remainingTicks, durationTicks);
        totalDuration = Math.max(1, remainingTicks);
    }

    public static void tickClient() {
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks == 0) {
                shakeIntensity = 0.0F;
            }
        }
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (remainingTicks <= 0 || shakeIntensity <= 0.001F) {
            return;
        }

        float partial = (float) event.getPartialTick();
        float progress = ((float) remainingTicks - partial) / (float) totalDuration;
        float dampedAmplitude = shakeIntensity * progress * progress;

        float time = (totalDuration - remainingTicks) + partial;
        float pitchOffset = Mth.sin(time * 2.4F) * dampedAmplitude * 2.8F;
        float yawOffset = Mth.cos(time * 1.9F) * dampedAmplitude * 2.2F;
        float rollOffset = Mth.sin(time * 3.1F) * dampedAmplitude * 3.5F;

        // v2.4: Clamping angular estricto para evitar inversión vertical de cámara
        event.setPitch(Mth.clamp(event.getPitch() + pitchOffset, -89.0F, 89.0F));
        event.setYaw(event.getYaw() + yawOffset);
        event.setRoll(Mth.clamp(event.getRoll() + rollOffset, -25.0F, 25.0F));
    }
}
