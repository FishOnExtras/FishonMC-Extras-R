package dannypx.foe.mixin.inject;

import dannypx.foe.handler.renderer.PlayerHatRenderHandler;
import dannypx.foe.handler.renderer.RodRendererHandler;
import dannypx.foe.interfaces.IHatSettingsHolder;
import dannypx.foe.interfaces.IRodSettingsHolder;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements IHatSettingsHolder, IRodSettingsHolder {

    @Unique
    private float hatHeight = PlayerHatRenderHandler.DEFAULT_HEIGHT;

    @Unique
    private float hatScale = PlayerHatRenderHandler.DEFAULT_SCALE;

    @Override
    public float foer$getHatHeight() {
        return hatHeight;
    }

    @Override
    public void foer$setHatHeight(float height) {
        this.hatHeight = height;
    }

    @Override
    public float foer$getHatScale() {
        return hatScale;
    }

    @Override
    public void foer$setHatScale(float scale) {
        this.hatScale = scale;
    }

    @Unique
    private RodRendererHandler.RodSettings rodSettings = RodRendererHandler.RodSettings.DEFAULT;

    @Override
    public RodRendererHandler.RodSettings foer$getRodSettings() {
        return rodSettings;
    }

    @Override
    public void foer$setRodSettings(RodRendererHandler.RodSettings rodSettings) {
        this.rodSettings = rodSettings;
    }
}
