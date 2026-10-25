package dannypx.foe.handler.logic;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.particle.RippleParticle;
import dannypx.foe.type.tuple.Pair;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class ParticleHandler extends Handler {
    private static ParticleHandler INSTANCE = new ParticleHandler();

    public static ParticleHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ParticleHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public static final SimpleParticleType RIPPLE = FabricParticleTypes.simple();
    //endregion

    //region Methods
    @Override
    public void init() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "ripple"), RIPPLE);
        ParticleFactoryRegistry.getInstance().register(RIPPLE, RippleParticle.Provider::new);
    }
    //endregion

    //region Dev

    /// Field, Pair<Value, Tooltip>
    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "key", Pair.of(Component.literal("value"), Component.empty())
        );
    }
    //endregion
}
