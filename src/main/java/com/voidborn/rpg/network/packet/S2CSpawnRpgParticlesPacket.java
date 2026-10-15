package com.voidborn.rpg.network.packet;

import com.voidborn.rpg.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Random;

public class S2CSpawnRpgParticlesPacket {
    public enum Pattern {
        CLEAVE_ARC,
        SHADOW_DASH_TRAIL,
        CRITICAL_BURST,
        ULTIMATE_CHARGE_VORTEX,
        ULTIMATE_DETONATION_SHOCKWAVE
    }

    private final Pattern pattern;
    private final double x;
    private final double y;
    private final double z;
    private final double dirX;
    private final double dirY;
    private final double dirZ;
    private final float radius;
    private final int count;

    public S2CSpawnRpgParticlesPacket(
            Pattern pattern,
            double x, double y, double z,
            double dirX, double dirY, double dirZ,
            float radius, int count
    ) {
        this.pattern = pattern;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
        this.radius = radius;
        this.count = count;
    }

    public static void encode(S2CSpawnRpgParticlesPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.pattern);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.dirX);
        buf.writeDouble(msg.dirY);
        buf.writeDouble(msg.dirZ);
        buf.writeFloat(msg.radius);
        buf.writeVarInt(msg.count);
    }

    public static S2CSpawnRpgParticlesPacket decode(FriendlyByteBuf buf) {
        return new S2CSpawnRpgParticlesPacket(
                buf.readEnum(Pattern.class),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readFloat(), buf.readVarInt()
        );
    }

    public static void handle(S2CSpawnRpgParticlesPacket msg, CustomPayloadEvent.Context ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Random rand = new Random();

        switch (msg.pattern) {
            case CLEAVE_ARC -> {
                double baseYaw = Math.atan2(msg.dirZ, msg.dirX);
                double arcSpan = Math.toRadians(140.0D);
                for (int i = 0; i < msg.count; i++) {
                    double progress = (double) i / Math.max(1, msg.count - 1);
                    double angle = baseYaw - (arcSpan * 0.5D) + (arcSpan * progress);
                    double dist = msg.radius * (0.75D + rand.nextDouble() * 0.35D);
                    double px = msg.x + Math.cos(angle) * dist;
                    double pz = msg.z + Math.sin(angle) * dist;
                    double py = msg.y + (progress - 0.5D) * 0.45D;

                    double vx = Math.cos(angle) * 0.28D;
                    double vz = Math.sin(angle) * 0.28D;
                    level.addParticle(ModParticles.VOID_SLASH.get(), true, px, py, pz, vx, 0.02D, vz);
                }
            }
            case SHADOW_DASH_TRAIL -> {
                for (int i = 0; i < msg.count; i++) {
                    double t = (double) i / Math.max(1, msg.count);
                    double px = msg.x + msg.dirX * msg.radius * t;
                    double py = msg.y + msg.dirY * msg.radius * t + Math.sin(t * Math.PI * 4.0D) * 0.35D;
                    double pz = msg.z + msg.dirZ * msg.radius * t;
                    level.addParticle(ModParticles.SHADOW_VORTEX.get(), true,
                            px, py, pz,
                            (rand.nextDouble() - 0.5D) * 0.18D,
                            0.08D,
                            (rand.nextDouble() - 0.5D) * 0.18D);
                }
            }
            case CRITICAL_BURST -> {
                for (int i = 0; i < msg.count; i++) {
                    float theta = rand.nextFloat() * Mth.TWO_PI;
                    float phi = (rand.nextFloat() - 0.5F) * Mth.PI;
                    double speed = 0.25D + rand.nextDouble() * 0.35D;
                    double vx = Mth.cos(theta) * Mth.cos(phi) * speed;
                    double vy = Mth.sin(phi) * speed + 0.1D;
                    double vz = Mth.sin(theta) * Mth.cos(phi) * speed;

                    if (i % 2 == 0) {
                        level.addParticle(ModParticles.VOID_SLASH.get(), true, msg.x, msg.y, msg.z, vx, vy, vz);
                    } else {
                        level.addParticle(ModParticles.SHADOW_VORTEX.get(), true, msg.x, msg.y, msg.z, vx * 0.6D, vy, vz * 0.6D);
                    }
                }
            }
            case ULTIMATE_CHARGE_VORTEX -> {
                for (int i = 0; i < msg.count; i++) {
                    double angle = (Mth.TWO_PI * i) / msg.count + (rand.nextDouble() * 0.3D);
                    double dist = 1.2D + rand.nextDouble() * msg.radius;
                    double px = msg.x + Math.cos(angle) * dist;
                    double pz = msg.z + Math.sin(angle) * dist;
                    double py = msg.y - 0.5D + rand.nextDouble() * 2.5D;

                    double tangentX = -Math.sin(angle) * 0.32D;
                    double tangentZ = Math.cos(angle) * 0.32D;
                    level.addParticle(ModParticles.SHADOW_VORTEX.get(), true, px, py, pz, tangentX, 0.14D, tangentZ);
                }
            }
            case ULTIMATE_DETONATION_SHOCKWAVE -> {
                int ringParticles = (int) (msg.count * 0.65F);
                int pillarParticles = msg.count - ringParticles;

                for (int i = 0; i < ringParticles; i++) {
                    double angle = (Mth.TWO_PI * i) / ringParticles;
                    double speed = 0.45D + (i % 3) * 0.22D;
                    double vx = Math.cos(angle) * speed;
                    double vz = Math.sin(angle) * speed;
                    level.addParticle(ModParticles.VOID_SLASH.get(), true,
                            msg.x + Math.cos(angle) * 0.8D,
                            msg.y + 0.35D,
                            msg.z + Math.sin(angle) * 0.8D,
                            vx, 0.05D, vz);
                }

                for (int i = 0; i < pillarParticles; i++) {
                    double angle = rand.nextDouble() * Mth.TWO_PI;
                    double r = rand.nextDouble() * (msg.radius * 0.55D);
                    double px = msg.x + Math.cos(angle) * r;
                    double pz = msg.z + Math.sin(angle) * r;
                    double py = msg.y + rand.nextDouble() * 1.5D;
                    level.addParticle(ModParticles.SHADOW_VORTEX.get(), true,
                            px, py, pz,
                            -Math.sin(angle) * 0.45D,
                            0.28D + rand.nextDouble() * 0.35D,
                            Math.cos(angle) * 0.45D);
                }
            }
        }
    }
}
