package dannypx.foe.screens.widget;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.helper.ItemStackHelper;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.screens.element.BoxColorElement;
import dannypx.foe.screens.element.Element;
import dannypx.foe.type.StringStyle;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SmallButtonColorWidget extends AbstractWidget {
    Minecraft minecraft = Minecraft.getInstance();

    private final SmallButtonBuilder.Callback callback;

    Pattern PATTERN = Pattern.compile("^(?:([a-z_]+:[a-z_]+)(?:\\[(.*)\\])?|(.+))$");

    private Component icon;
    private final boolean smallCaps;

    Pair<String, Element> box;
    Pair<String, Element> box_hover;

    List<Pair<String, Element>> elements = new ArrayList<>();

    public SmallButtonColorWidget(int x, int y, int width, int height, Component icon, boolean smallCaps, int color, @Nullable Tooltip tooltip, Component message, SmallButtonBuilder.Callback callback) {
        super(x, y, width, height, message);
        this.icon = icon;
        this.smallCaps = smallCaps;
        this.callback = callback;
        this.setTooltip(tooltip);
        this.init();

        box = Pair.of("button_box", new BoxColorElement(getX(),
                getY(),
                width, height, true, false, color));

        box_hover = Pair.of("button_hover_box", new BoxColorElement(getX(),
                getY(),
                width, height, true, true, color));
    }

    private void init() {
        this.initElements();
    }

    private void initElements() {
        elements.clear();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBox(guiGraphics);
        this.renderIcon(guiGraphics);
        this.requestCursor(guiGraphics);
    }

    private void requestCursor(@NotNull GuiGraphics guiGraphics) {
        if(this.isHovered()) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
    }

    private void renderBox(GuiGraphics guiGraphics) {
        (isHovered ? box_hover : box).value2().render(guiGraphics);
    }

    private void renderIcon(GuiGraphics guiGraphics) {
        Matcher m = PATTERN.matcher(icon.getString());

        if (m.matches()) {
            if(m.group(1) != null) {
                if(minecraft.player != null) {
                    ItemStack itemStack = ItemStackHelper.valueOf(icon.getString());

                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().translate(getX() + ((float) width / 2) - 6, getY() + ((float) height / 2) - 6);
                    guiGraphics.pose().scale(12f / 16f, 12f / 16f);

                    guiGraphics.renderItem(itemStack, 0, 0);

                    guiGraphics.pose().popMatrix();
                }
            } else {
                int stringWidth = smallCaps ? minecraft.font.width(TextHelper.smallCaps(icon)) : minecraft.font.width(icon);
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(0.0f, 0.0f);

                if(smallCaps) {
                    GuiGraphicsHelper.drawString(guiGraphics,
                            minecraft.font,
                            icon,
                            getX() + (width / 2) - stringWidth / 2, getY() + (height / 2) - minecraft.font.lineHeight / 2,
                            StringStyle.SHADOW, StringStyle.MIDDLE, StringStyle.SMALL_CAPS
                    );
                } else {
                    GuiGraphicsHelper.drawString(guiGraphics,
                            minecraft.font,
                            icon,
                            getX() + (width / 2) - stringWidth / 2, getY() + (height / 2) - minecraft.font.lineHeight / 2,
                            StringStyle.SHADOW
                    );
                }

                guiGraphics.pose().popMatrix();
            }
        }
    }

    @Override
    public void setPosition(int i, int j) {
        super.setPosition(i, j);
        box.value2().setPosition(i, j);
        box_hover.value2().setPosition(i, j);
    }

    @Override
    public void setSize(int i, int j) {
        super.setSize(i, j);
        box.value2().setSize(i, j);
        box_hover.value2().setSize(i, j);
    }

    @Override
    public void setMessage(Component component) {
        super.setMessage(component);
        this.icon = component;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    @Override
    public void onClick(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubled) {
        super.onClick(mouseButtonEvent, doubled);
        if(callback != null) {
            this.callback.onClick(this);
        }
    }
}
