package dannypx.foe.interfaces;

import dannypx.foe.handler.renderer.RodRendererHandler;

public interface IRodSettingsHolder {
    RodRendererHandler.RodSettings foer$getRodSettings();
    void foer$setRodSettings(RodRendererHandler.RodSettings rodSettings);
}
