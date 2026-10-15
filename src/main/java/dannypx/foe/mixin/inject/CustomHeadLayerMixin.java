package dannypx.foe.mixin.inject;

import com.mojang.blaze3d.vertex.PoseStack;
import dannypx.foe.handler.renderer.PlayerHatRenderHandler;
import dannypx.foe.interfaces.IHatHeightHolder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> {
    @Unique
    private static final float HAT_PIVOT_Y = 0.25f;

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HeadedModel;translateToHead(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void injectSubmit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S livingEntityRenderState, float f, float g, CallbackInfo ci) {
        IHatHeightHolder holder = (IHatHeightHolder) livingEntityRenderState;
        float height = holder.foer$getHatHeight();
        float scale = holder.foer$getHatScale();
        if(height == PlayerHatRenderHandler.DEFAULT_HEIGHT && scale == PlayerHatRenderHandler.DEFAULT_SCALE) return;

        poseStack.translate(0.0f, -(height - 1.0f) * 0.5f, 0.0f);

        if(scale != PlayerHatRenderHandler.DEFAULT_SCALE) {
            poseStack.translate(0.0f, -HAT_PIVOT_Y, 0.0f);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0f, HAT_PIVOT_Y, 0.0f);
        }
    }
}
