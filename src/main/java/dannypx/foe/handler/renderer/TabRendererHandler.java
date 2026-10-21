package dannypx.foe.handler.renderer;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.ScoreboardHandler;
import dannypx.foe.handler.logic.CrewHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.StringStyle;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import dannypx.foe.screens.element.BoxElement;
import dannypx.foe.screens.element.Element;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TabRendererHandler extends Handler {
    private static TabRendererHandler INSTANCE = new TabRendererHandler();

    public static TabRendererHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new TabRendererHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public void renderCrewTab(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, int indexPlayerEntry, List<PlayerInfo> playerEntries) {
        if(color == minecraft.options.getBackgroundColor(553648127)) {
            int index = indexPlayerEntry + 1 >= playerEntries.size() ? 0 : indexPlayerEntry + 1;

            if(!ScoreboardHandler.instance().getCrew().getString().isBlank()
                    && ProfileDataHandler.instance().getProfileData().hasImportedCrew
                    && !CrewHandler.instance().getOnlineMembers().isEmpty()
                    && index == 0
                    && Configs.rendererConfig.showOnlineCrewMembers.get()
            ) {
                // Header Crew Name
                int height = 16;
                int width = 40;

                Element crewBox = new BoxElement(x1, y1 - (height - 5) - 1, -1, width, height, true, false, true, true, false, true);
                crewBox.render(guiGraphics);

                Component crewComponent = Component.literal(ScoreboardHandler.instance().getCrew().getString());

                GuiGraphicsHelper.drawString(
                        guiGraphics, minecraft.font, crewComponent,
                        x1 + width / 2 - TextHelper.getWidth(minecraft.font, crewComponent, true) / 2,
                        y1 - (height - 5) + (height - 5) / 2 - minecraft.font.lineHeight / 2 + 1,
                        StringStyle.SHADOW, StringStyle.MIDDLE, StringStyle.SMALL_CAPS
                );

                // Left Bar
                Element leftBar = new BoxElement(x1 - 5, y1 - 1, -1, 5, CrewHandler.instance().getOnlineMembers().size() * 9 + 1, true, false, true, false, true, true);
                leftBar.render(guiGraphics);

                int gradientWidth = 150;

                // Box
                GuiGraphicsHelper.drawHorizontalGradient(guiGraphics, x1, y1, x1 + gradientWidth, y1 + CrewHandler.instance().getOnlineMembers().size() * 9 - 1, 0x88FFAA00, 0x00FFAA00);

                // Border
                guiGraphics.vLine(x1 - 1, y1 - 1, y1 + CrewHandler.instance().getOnlineMembers().size() * 9 - 1, 0xFF000000);
                GuiGraphicsHelper.drawHorizontalGradient(guiGraphics, x1 - 1, y1 - 1, x1 + gradientWidth, y1, 0xFF000000, 0x00000000);
                GuiGraphicsHelper.drawHorizontalGradient(guiGraphics, x1 - 1, y1 + CrewHandler.instance().getOnlineMembers().size() * 9 - 1, x1 + gradientWidth, y1 + CrewHandler.instance().getOnlineMembers().size() * 9, 0xFF000000, 0x00000000);
            }
        }
    }
    //endregion

    //region Methods
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
