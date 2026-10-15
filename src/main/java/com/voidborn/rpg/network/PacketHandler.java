package com.voidborn.rpg.network;

import com.voidborn.rpg.VoidbornRpgMod;
import com.voidborn.rpg.network.packet.C2SSpecialAttackPacket;
import com.voidborn.rpg.network.packet.C2SUltimateSkillPacket;
import com.voidborn.rpg.network.packet.S2CCameraShakePacket;
import com.voidborn.rpg.network.packet.S2CPlayerAnimationPacket;
import com.voidborn.rpg.network.packet.S2CSpawnRpgParticlesPacket;
import com.voidborn.rpg.network.packet.S2CSyncCombatDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {
    private static final int PROTOCOL_VERSION = 1;

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(VoidbornRpgMod.id("main_channel"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, version) -> version == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, version) -> version == PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        int packetId = 0;

        CHANNEL.messageBuilder(C2SSpecialAttackPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SSpecialAttackPacket::encode)
                .decoder(C2SSpecialAttackPacket::decode)
                .consumerMainThread(C2SSpecialAttackPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SUltimateSkillPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SUltimateSkillPacket::encode)
                .decoder(C2SUltimateSkillPacket::decode)
                .consumerMainThread(C2SUltimateSkillPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncCombatDataPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncCombatDataPacket::encode)
                .decoder(S2CSyncCombatDataPacket::decode)
                .consumerMainThread(S2CSyncCombatDataPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CPlayerAnimationPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPlayerAnimationPacket::encode)
                .decoder(S2CPlayerAnimationPacket::decode)
                .consumerMainThread(S2CPlayerAnimationPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CCameraShakePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CCameraShakePacket::encode)
                .decoder(S2CCameraShakePacket::decode)
                .consumerMainThread(S2CCameraShakePacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSpawnRpgParticlesPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSpawnRpgParticlesPacket::encode)
                .decoder(S2CSpawnRpgParticlesPacket::decode)
                .consumerMainThread(S2CSpawnRpgParticlesPacket::handle)
                .add();
    }

    public static void sendToServer(Object packet) {
        CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToTrackingAndSelf(Entity entity, Object packet) {
        CHANNEL.send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity));
    }

    public static void sendToNear(ServerLevel level, Vec3 pos, double radius, Object packet) {
        CHANNEL.send(packet, PacketDistributor.NEAR.with(
                new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, level.dimension())
        ));
    }
}
