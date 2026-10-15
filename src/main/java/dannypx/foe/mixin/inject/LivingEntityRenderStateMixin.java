package dannypx.foe.mixin.inject;

import dannypx.foe.handler.renderer.PlayerHatRenderHandler;
import dannypx.foe.interfaces.IHatHeightHolder;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements IHatHeightHolder {

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
}
