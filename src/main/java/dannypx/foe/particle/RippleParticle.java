package dannypx.foe.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

public class RippleParticle extends SingleQuadParticle {
    private static final int LIFETIME_TICKS = 60;
    private static final float START_SIZE = 0.08F;
    private static final float END_SIZE = 0.75F;
    private static final float MAX_ALPHA = 0.85F;
    private static final float TOTAL_SPIN_RADIANS = 0.9F;
    private static final float HALF_PI = (float) (Math.PI / 2.0D);

    private final float startAngle;
    private final float spinDirection;

    protected RippleParticle(ClientLevel clientLevel, double x, double y, double z, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, x, y, z, textureAtlasSprite);
        this.lifetime = LIFETIME_TICKS;
        this.hasPhysics = false;
        this.quadSize = START_SIZE;
        this.startAngle = this.random.nextFloat() * (float) Math.PI;
        this.spinDirection = this.random.nextBoolean() ? 1.0F : -1.0F;
    }

    private float easedProgress(float partialTick) {
        float progress = Math.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        return 1.0F - (1.0F - progress) * (1.0F - progress);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return Mth.lerp(easedProgress(partialTick), START_SIZE, END_SIZE);
    }

    @Override
    public void extract(@NonNull QuadParticleRenderState quadParticleRenderState, @NonNull Camera camera, float partialTick) {
        float progress = Math.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        this.alpha = MAX_ALPHA * (1.0F - progress * progress);

        float angle = this.startAngle + this.spinDirection * TOTAL_SPIN_RADIANS * easedProgress(partialTick);
        Quaternionf rotation = new Quaternionf().rotationX(-HALF_PI).rotateZ(angle);
        this.extractRotatedQuad(quadParticleRenderState, camera, rotation, partialTick);
    }

    @Override
    protected SingleQuadParticle.@NonNull Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public record Provider(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NonNull SimpleParticleType particleType, @NonNull ClientLevel clientLevel, double x, double y, double z, double xd, double yd, double zd, @NonNull RandomSource random) {
            return new RippleParticle(clientLevel, x, y, z, this.spriteSet.get(random));
        }
    }
}
