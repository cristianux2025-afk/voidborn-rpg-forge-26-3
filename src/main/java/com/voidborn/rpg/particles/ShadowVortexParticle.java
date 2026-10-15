package com.voidborn.rpg.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class ShadowVortexParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double centerX;
    private final double centerZ;
    private double orbitAngle;
    private final double orbitRadius;
    private final double angularSpeed;
    private final double verticalAscent;

    protected ShadowVortexParticle(
            ClientLevel level,
            double x, double y, double z,
            double vx, double vy, double vz,
            SpriteSet sprites
    ) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.hasPhysics = false;

        this.orbitRadius = Math.max(0.35D, Math.sqrt(vx * vx + vz * vz) * 4.5D);
        this.orbitAngle = Math.atan2(vz, vx);
        this.centerX = x - Math.cos(this.orbitAngle) * this.orbitRadius;
        this.centerZ = z - Math.sin(this.orbitAngle) * this.orbitRadius;
        this.angularSpeed = 0.22D + this.random.nextDouble() * 0.14D;
        this.verticalAscent = vy != 0.0D ? vy : (0.06D + this.random.nextDouble() * 0.08D);

        this.lifetime = 22 + this.random.nextInt(12);
        this.quadSize = 0.22F + this.random.nextFloat() * 0.18F;

        this.rCol = 0.38F;
        this.gCol = 0.08F;
        this.bCol = 0.52F;
        this.alpha = 0.90F;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float) this.age / (float) this.lifetime;

        this.orbitAngle += this.angularSpeed;
        double currentRadius = this.orbitRadius * (1.0D - progress * 0.65D);

        double targetX = this.centerX + Math.cos(this.orbitAngle) * currentRadius;
        double targetZ = this.centerZ + Math.sin(this.orbitAngle) * currentRadius;
        double targetY = this.y + this.verticalAscent * (1.0D - progress * 0.4D);

        this.setPos(targetX, targetY, targetZ);
        this.roll += 0.25F;

        this.alpha = progress < 0.2F
                ? (progress / 0.2F) * 0.9F
                : Mth.clamp((1.0F - progress) * 1.1F, 0.0F, 0.9F);
        this.quadSize *= 0.985F;

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double vx, double vy, double vz
        ) {
            return new ShadowVortexParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
