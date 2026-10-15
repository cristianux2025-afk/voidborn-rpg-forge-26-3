package com.voidborn.rpg.network.packet;

import com.voidborn.rpg.client.CameraShakeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class S2CCameraShakePacket {
    private final double originX;
    private final double originY;
    private final double originZ;
    private final float maxRadius;
    private final float baseIntensity;
    private final int durationTicks;

    public S2CCameraShakePacket(Vec3 origin, float maxRadius, float baseIntensity, int durationTicks) {
        this(origin.x, origin.y, origin.z, maxRadius, baseIntensity, durationTicks);
    }

    public S2CCameraShakePacket(
            double originX,
            double originY,
            double originZ,
            float maxRadius,
            float baseIntensity,
            int durationTicks
    ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.maxRadius = maxRadius;
        this.baseIntensity = baseIntensity;
        this.durationTicks = durationTicks;
    }

    public static void encode(S2CCameraShakePacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.originX);
        buf.writeDouble(msg.originY);
        buf.writeDouble(msg.originZ);
        buf.writeFloat(msg.maxRadius);
        buf.writeFloat(msg.baseIntensity);
        buf.writeVarInt(msg.durationTicks);
    }

    public static S2CCameraShakePacket decode(FriendlyByteBuf buf) {
        return new S2CCameraShakePacket(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt()
        );
    }

    public static void handle(S2CCameraShakePacket msg, CustomPayloadEvent.Context ctx) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        double distance = player.position().distanceTo(new Vec3(msg.originX, msg.originY, msg.originZ));
        if (distance <= msg.maxRadius) {
            float attenuation = (float) Math.max(0.0D, 1.0D - (distance / msg.maxRadius));
            CameraShakeHandler.triggerShake(msg.baseIntensity * attenuation, msg.durationTicks);
        }
    }
}
