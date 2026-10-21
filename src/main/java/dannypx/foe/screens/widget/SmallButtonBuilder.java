package dannypx.foe.screens.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class SmallButtonBuilder {
    private int x;
    private int y;
    private int width = 200;
    private int height = 20;
    private int color = -1;
    private Component icon = Component.empty();
    private boolean smallCaps = true;
    private Tooltip tooltip = null;
    private Callback callback;

    public enum Style {NO_SMALL_CAPS}

    public SmallButtonBuilder(Callback callback) {
        this.callback = callback;
    }

    public SmallButtonBuilder(int x, int y, SmallButtonBuilder.Callback callback) {
        this.x = x;
        this.y = y;
        this.callback = callback;
    }

    public SmallButtonBuilder(int x, int y, int width, int height, SmallButtonBuilder.Callback callback) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.callback = callback;
    }

    public SmallButtonBuilder size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public SmallButtonBuilder position(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public SmallButtonBuilder width(int width) {
        this.width = width;
        return this;
    }

    public SmallButtonBuilder height(int height) {
        this.height = height;
        return this;
    }

    public SmallButtonBuilder color(int color) {
        this.color = color;
        return this;
    }

    public SmallButtonBuilder icon(String icon) {
        this.icon = Component.literal(icon);
        return this;
    }

    public SmallButtonBuilder icon(Component icon) {
        this.icon = icon;
        return this;
    }

    public SmallButtonBuilder style(Style ...styles) {
        for (Style style : styles) {
            switch (style) {
                case NO_SMALL_CAPS -> this.smallCaps = false;
            }
        }
        return this;
    }

    public SmallButtonBuilder tooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public interface Callback {
        void onClick(AbstractWidget button);
    }

    public AbstractWidget build() {
        if(color == -1) {
            return new SmallButtonWidget(this.x, this.y, this.width, this.height, this.icon, this.smallCaps, this.tooltip, this.icon, this.callback);
        } else {
            return new SmallButtonColorWidget(this.x, this.y, this.width, this.height, this.icon, this.smallCaps, this.color, this.tooltip, this.icon, this.callback);
        }
    }
}
