package dannypx.foe.mixin.inject;

import dannypx.foe.interfaces.IFishPoseState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements IFishPoseState {
    @Unique
    private boolean holdingFish;

    @Override
    public boolean foer$isHoldingFish() {
        return holdingFish;
    }

    @Override
    public void foer$setHoldingFish(boolean value) {
        holdingFish = value;
    }
}
