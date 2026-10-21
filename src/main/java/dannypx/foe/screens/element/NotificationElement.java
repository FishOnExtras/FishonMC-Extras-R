package dannypx.foe.screens.element;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.screens.interfaces.ScreenConstants;
import java.util.List;
import java.util.Objects;

import dannypx.foe.type.StringStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class NotificationElement extends Element implements ScreenConstants {
    //region Fields
    private final Minecraft minecraft;
    private final Font font;
    private int x = 0;
    private int y = 0;
    private final ItemStack itemStack;
    private final int rows;
    private final int columns;
    private final List<Component> componentList;

    private final int contentHeight;
    private final int contentWidth;

    private static final Identifier BOX_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "textures/gui/sprites/elements/box_atlas.png");
    private static final int LINE_HEIGHT = 1;
    private static final int BOX_PADDING = 5 + PADDING_QUART;

    private boolean isError = false;
    //endregion

    public NotificationElement(Minecraft minecraft, int width, ItemStack itemStack, int rows, int columns, List<Component> componentList) {
        super(0,
                0,
                0,
                0,
                null,
                Component.literal("Notification Element"),
                false);
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.itemStack = itemStack;
        this.rows = rows;
        this.columns = columns;
        this.componentList = componentList;

        contentHeight = Math.max(16 + PADDING_QUART * 2, rows * (font.lineHeight + LINE_HEIGHT) - LINE_HEIGHT);
        this.height = BOX_PADDING * 2 + contentHeight;


        this.width = width;
        contentWidth = width - BOX_PADDING * 2;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    //region Methods
    @Override
    public void render(GuiGraphics guiGraphics) {
        if(isError) {
            return;
        }
        this.renderBox(guiGraphics);
        this.renderItem(guiGraphics, itemStack);
        this.renderComponent(guiGraphics, font, componentList, rows, columns);
    }

    private void renderComponent(GuiGraphics guiGraphics, Font font, List<Component> componentList, int rows, int columns) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int componentX = itemStack.isEmpty()
                        ? x + BOX_PADDING + (contentWidth / columns) * column
                        : x + BOX_PADDING + 16 + PADDING_QUART + ((contentWidth - (16 + PADDING_QUART)) / columns) * column;
                int componentY = rows == 1
                        ? y + this.height / 2 - font.lineHeight / 2
                        : y + BOX_PADDING + (font.lineHeight + LINE_HEIGHT) * row;

                if((row * columns) + column + 1 > componentList.size()) {
                    LoggerHandler.error("Index" + ((row * columns) + column) + " out of bounds for length " + componentList.size());
                    isError = true;
                    return;
                }

                Component component = componentList.get((row * columns) + column);

                if(!Objects.equals(component, Component.empty())) {
                    GuiGraphicsHelper.drawString(guiGraphics, font, component,
                            componentX, componentY,
                            StringStyle.SHADOW, StringStyle.MIDDLE, StringStyle.HAS_CUSTOM_FONT, StringStyle.SMALL_CAPS
                    );
                }
            }
        }
    }

    private void renderItem(GuiGraphics guiGraphics, ItemStack itemStack) {
        if(!itemStack.isEmpty()) {
            guiGraphics.renderItem(itemStack, x + BOX_PADDING, y + this.height / 2 - 16 / 2);
        }
    }

    private void renderBox(GuiGraphics guiGraphics) {
        // Top Left
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                this.x, this.y,
                0, 0,
                5, 5,
                5, 5,
                15,15
        );

        // Top
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                this.x + 5, this.y,
                5, 0,
                this.width - 10, 5,
                5, 5,
                15, 15
        );

        // Top Right
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                x + this.width - 5, this.y,
                10, 0,
                5, 5,
                5, 5,
                15, 15
        );

        // Centre Left
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                this.x, this.y + 5,
                0, 5,
                5, this.height - 10,
                5, 5,
                15,15
        );

        // Centre
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                this.x + 5, this.y + 5,
                5, 5,
                this.width - 10, this.height - 10,
                5, 5,
                15, 15
        );

        // Centre Right
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                x + this.width - 5, this.y + 5,
                10, 5,
                5, this.height - 10,
                5, 5,
                15, 15
        );

        // Bottom Left
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                this.x, y + this.height - 5,
                0, 10,
                5, 5,
                5, 5,
                15,15
        );

        // Bottom
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                this.x + 5, y + this.height - 5,
                5, 10,
                this.width - 10, 5,
                5, 5,
                15, 15
        );

        // Bottom Right
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                x + this.width - 5, y + this.height - 5,
                10, 10,
                5, 5,
                5, 5,
                15, 15
        );
    }
    //endregion
}
