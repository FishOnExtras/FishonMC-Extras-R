package dannypx.foe.mixin.inject;

import com.mojang.blaze3d.vertex.PoseStack;
import dannypx.foe.config.Configs;
import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.renderer.RodRendererHandler;
import dannypx.foe.interfaces.IRodSettingsHolder;
import dannypx.foe.item.ValidateItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<S extends ArmedEntityRenderState> {
    @Unique
    private static RodRendererHandler.RodSettings getRodSettings(ArmedEntityRenderState armedEntityRenderState, ItemStack itemStack) {
        if(!itemStack.is(Items.FISHING_ROD)) return null;
        RodRendererHandler.RodSettings settings = ((IRodSettingsHolder) armedEntityRenderState).foer$getRodSettings();
        return settings.isDefault() ? null : settings;
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void injectSubmitArmWithItemHead(S armedEntityRenderState, ItemStackRenderState itemStackRenderState, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, CallbackInfo ci) {
        RodRendererHandler.RodSettings settings = getRodSettings(armedEntityRenderState, itemStack);
        if(settings == null) return;
        float side = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.pushPose();
        poseStack.translate(-side * settings.offsetRight(), -settings.offsetUp(), -settings.offsetForward());

        if (ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && !Configs.rendererConfig.showPet.get()
                && armedEntityRenderState instanceof ArmorStandRenderState
                && Configs.mixinConfig.itemInHandLayerMixinSubmitArmWithItem.get()
                && ValidateItem.isPet(itemStack).value1()
        ) {
            poseStack.popPose();
            ci.cancel();
        }
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void injectSubmitArmWithItemSubmit(S armedEntityRenderState, ItemStackRenderState itemStackRenderState, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, CallbackInfo ci) {
        RodRendererHandler.RodSettings settings = getRodSettings(armedEntityRenderState, itemStack);
        if(settings == null) return;
        poseStack.scale(settings.scale(), settings.scale(), settings.scale());
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"))
    private void injectSubmitArmWithItemReturn(S armedEntityRenderState, ItemStackRenderState itemStackRenderState, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, CallbackInfo ci) {
        if(getRodSettings(armedEntityRenderState, itemStack) == null) return;
        poseStack.popPose();
    }
}
