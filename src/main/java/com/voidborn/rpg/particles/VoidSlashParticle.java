package com.voidborn.rpg.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class VoidSlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float angularVelocity;
    private final float initialQuadSize;

    protected VoidSlashParticle(
            ClientLevel level,
            double x, double y, double z,
            double vx, double vy, double vz,
            SpriteSet sprites
    ) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.friction = 0.84F;
        this.gravity = -0.015F;
        this.lifetime = 14 + this.random.nextInt(6);
        this.initialQuadSize = 0.55F + this.random.nextFloat() * 0.35F;
        this.quadSize = this.initialQuadSize * 0.4F;

        this.roll = (float) (this.random.nextFloat() * Math.PI * 2.0D);
        this.oRoll = this.roll;
        this.angularVelocity = (this.random.nextBoolean() ? 1.0F : -1.0F) * (0.18F + this.random.nextFloat() * 0.14F);

        this.rCol = 0.72F;
        this.gCol = 0.28F;
        this.bCol = 1.00F;
        this.alpha = 0.95F;

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

        float lifeProgress = (float) this.age / (float) this.lifetime;

        if (lifeProgress < 0.3F) {
            this.quadSize = Mth.lerp(lifeProgress / 0.3F, this.initialQuadSize * 0.4F, this.initialQuadSize * 1.45F);
        } else {
            this.quadSize = Mth.lerp((lifeProgress - 0.3F) / 0.7F, this.initialQuadSize * 1.45F, 0.08F);
        }

        this.rCol = Mth.lerp(lifeProgress, 0.72F, 0.25F);
        this.gCol = Mth.lerp(lifeProgress, 0.28F, 0.02F);
        this.bCol = Mth.lerp(lifeProgress, 1.00F, 0.55F);
        this.alpha = Mth.clamp(1.0F - (lifeProgress * lifeProgress), 0.0F, 1.0F);

        this.roll += this.angularVelocity * (1.0F - lifeProgress * 0.5F);
        this.yd -= 0.04D * (double) this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

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
            return new VoidSlashParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
