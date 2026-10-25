package dannypx.foe.mixin.inject;

import dannypx.foe.config.Configs;
import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.logic.TextDisplayHandler;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class TextDisplayRendererMixin {
    @Shadow
    protected abstract Display.TextDisplay.CachedInfo splitLines(Component text, int maxWidth);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("TAIL"))
    private void injectExtractRenderState(Display.TextDisplay textDisplay, TextDisplayEntityRenderState textDisplayEntityRenderState, float f, CallbackInfo ci) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.textDisplayRendererMixinExtractRenderState.get()
        ) {
            Display.TextDisplay.TextRenderState data = textDisplayEntityRenderState.textRenderState;
            if (data == null) return;

            TextDisplayHandler.Entry entry = TextDisplayHandler.instance().get(textDisplay, data.text(), data.lineWidth(), this::splitLines);
            if (entry.info() == null) return;

            textDisplayEntityRenderState.textRenderState = new Display.TextDisplay.TextRenderState(
                    entry.prefixed(),
                    data.lineWidth(),
                    data.textOpacity(),
                    data.backgroundColor(),
                    data.flags()
            );
            textDisplayEntityRenderState.cachedInfo = entry.info();
        }
    }
}
