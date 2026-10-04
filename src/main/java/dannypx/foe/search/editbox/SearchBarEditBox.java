package dannypx.foe.search.editbox;

import dannypx.foe.handler.logic.SearchHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.List;

public class SearchBarEditBox extends EditBox {
    private static final int MAX_LENGTH = 256;

    private static final float HELP_SCALE = 0.75f;
    private static final int HELP_PADDING = 4;
    private static final int HELP_MARGIN = 2;
    private static final int HELP_BACKGROUND = 0xF0100010;
    private static final int HELP_BORDER = 0xB05000FF;

    private final Font font;

    public SearchBarEditBox(Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.literal("Search Bar"));
        this.font = font;

        SearchHandler handler = SearchHandler.instance();
        this.setMaxLength(MAX_LENGTH);
        this.addFormatter(new SearchHighlighter(this));
        this.setHint(Component.literal("Search Item Names").withStyle(ChatFormatting.GRAY));

        this.setValue(handler.getLastInput());
        this.setResponder(handler::parseSearch);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);

        if (this.isHovered()) {
            this.renderHelp(guiGraphics);
        }
    }


    private void renderHelp(GuiGraphics guiGraphics) {
        List<Component> lines = SearchHelp.lines();
        int lineHeight = font.lineHeight + 1;
        int panelWidth = SearchHelp.width(font) + HELP_PADDING * 2;
        int panelHeight = lines.size() * lineHeight + HELP_PADDING * 2;

        int screenWidth = Math.round(Minecraft.getInstance().getWindow().getGuiScaledWidth() / HELP_SCALE);
        int screenHeight = Math.round(Minecraft.getInstance().getWindow().getGuiScaledHeight() / HELP_SCALE);

        int boxCenter = Math.round((this.getX() + this.getWidth() / 2f) / HELP_SCALE);
        int x = Math.clamp(boxCenter - panelWidth / 2, 0, Math.max(0, screenWidth - panelWidth));
        int below = Math.round((this.getY() + this.getHeight() + HELP_MARGIN) / HELP_SCALE);
        int y = below + panelHeight <= screenHeight
                ? below
                : Math.max(0, Math.round((this.getY() - HELP_MARGIN) / HELP_SCALE) - panelHeight);

        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        try {
            guiGraphics.pose().scale(HELP_SCALE, HELP_SCALE);

            guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, HELP_BACKGROUND);
            guiGraphics.hLine(x, x + panelWidth - 1, y, HELP_BORDER);
            guiGraphics.hLine(x, x + panelWidth - 1, y + panelHeight - 1, HELP_BORDER);
            guiGraphics.vLine(x, y, y + panelHeight - 1, HELP_BORDER);
            guiGraphics.vLine(x + panelWidth - 1, y, y + panelHeight - 1, HELP_BORDER);

            int textY = y + HELP_PADDING;
            for (Component line : lines) {
                guiGraphics.drawString(font, line, x + HELP_PADDING, textY, CommonColors.WHITE, true);
                textY += lineHeight;
            }
        } finally {
            guiGraphics.pose().popMatrix();
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        SearchHandler.instance().setFocused(this.isFocused());
    }
}
