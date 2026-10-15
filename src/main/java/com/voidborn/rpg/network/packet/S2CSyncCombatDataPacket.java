package com.voidborn.rpg.network.packet;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class S2CSyncCombatDataPacket {
    private final float currentMana;
    private final float maxMana;
    private final int specialCooldown;
    private final int ultimateCooldown;
    private final int comboStage;
    private final int ultimateCastTicks;

    public S2CSyncCombatDataPacket(
            float currentMana,
            float maxMana,
            int specialCooldown,
            int ultimateCooldown,
            int comboStage,
            int ultimateCastTicks
    ) {
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.specialCooldown = specialCooldown;
        this.ultimateCooldown = ultimateCooldown;
        this.comboStage = comboStage;
        this.ultimateCastTicks = ultimateCastTicks;
    }

    public static void encode(S2CSyncCombatDataPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.currentMana);
        buf.writeFloat(msg.maxMana);
        buf.writeVarInt(msg.specialCooldown);
        buf.writeVarInt(msg.ultimateCooldown);
        buf.writeVarInt(msg.comboStage);
        buf.writeVarInt(msg.ultimateCastTicks);
    }

    public static S2CSyncCombatDataPacket decode(FriendlyByteBuf buf) {
        return new S2CSyncCombatDataPacket(
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2CSyncCombatDataPacket msg, CustomPayloadEvent.Context ctx) {
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            PlayerCombatData data = PlayerCombatData.get(clientPlayer);
            data.applySyncFromServer(
                    msg.currentMana,
                    msg.maxMana,
                    msg.specialCooldown,
                    msg.ultimateCooldown,
                    msg.comboStage,
                    msg.ultimateCastTicks
            );
        }
    }
}
