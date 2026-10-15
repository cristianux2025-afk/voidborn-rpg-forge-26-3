package com.voidborn.rpg.network.packet;

import com.voidborn.rpg.client.anim.PlayerAnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class S2CPlayerAnimationPacket {
    private final int entityId;
    private final String animationName;
    private final int fadeTicks;
    private final boolean lockFirstPersonArms;

    public S2CPlayerAnimationPacket(int entityId, String animationName, int fadeTicks, boolean lockFirstPersonArms) {
        this.entityId = entityId;
        this.animationName = animationName;
        this.fadeTicks = fadeTicks;
        this.lockFirstPersonArms = lockFirstPersonArms;
    }

    public static void encode(S2CPlayerAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeUtf(msg.animationName, 64);
        buf.writeVarInt(msg.fadeTicks);
        buf.writeBoolean(msg.lockFirstPersonArms);
    }

    public static S2CPlayerAnimationPacket decode(FriendlyByteBuf buf) {
        return new S2CPlayerAnimationPacket(
                buf.readVarInt(),
                buf.readUtf(64),
                buf.readVarInt(),
                buf.readBoolean()
        );
    }

    public static void handle(S2CPlayerAnimationPacket msg, CustomPayloadEvent.Context ctx) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(msg.entityId);
        if (entity instanceof AbstractClientPlayer clientPlayer) {
            PlayerAnimationHandler.playAnimationOnClientPlayer(
                    clientPlayer,
                    msg.animationName,
                    msg.fadeTicks,
                    msg.lockFirstPersonArms
            );
        }
    }
}
