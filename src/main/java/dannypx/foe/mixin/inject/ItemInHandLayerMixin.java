package dannypx.foe.mixin.inject;

import com.mojang.blaze3d.vertex.PoseStack;
import dannypx.foe.config.Configs;
import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.item.ValidateItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<S extends ArmedEntityRenderState> {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void injectSubmitArmWithItem(S armedEntityRenderState, ItemStackRenderState itemStackRenderState, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, CallbackInfo ci) {
        if (ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && !Configs.rendererConfig.showPet.get()
                && armedEntityRenderState instanceof ArmorStandRenderState
                && Configs.mixinConfig.itemInHandLayerMixinSubmitArmWithItem.get()
                && ValidateItem.isPet(itemStack).value1()
        ) {
            ci.cancel();
        }
    }
}
