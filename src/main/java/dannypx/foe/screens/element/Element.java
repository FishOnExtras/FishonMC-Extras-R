package dannypx.foe.screens.element;

import dannypx.foe.type.Alignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class Element {
    public int width;
    public int height;
    public Alignment alignment;
    public float xPos;
    public float yPos;
    public Component message;
    public final boolean isCopy;

    protected Element(int width, int height, float xPos, float yPos, Alignment alignment, Component message, boolean isCopy) {
        this.width = width;
        this.height = height;
        this.xPos = xPos;
        this.yPos = yPos;
        this.alignment = alignment;
        this.message = message;
        this.isCopy = isCopy;
    }

    public void render(GuiGraphics guiGraphics) {}

    public void setXPercent(float percent) {
        xPos = percent;
    }

    public void setYPercent(float percent) {
        yPos = percent;
    }

    public void setPosition(int x, int y) {};

    public void setSize(int width, int height) {};
}
