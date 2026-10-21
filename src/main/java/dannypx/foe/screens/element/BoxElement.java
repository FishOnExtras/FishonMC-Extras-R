package dannypx.foe.screens.element;

import dannypx.foe.FishOnMCExtras;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class BoxElement extends Element {
    //region Fields
    private final Identifier BOX_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "textures/gui/sprites/elements/box_atlas.png");
    private final Identifier BOX_SOLID_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "textures/gui/sprites/elements/box_solid_atlas.png");
    private final Identifier BOX_ALT_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "textures/gui/sprites/elements/box_alt_atlas.png");
    private final Identifier BOX_SOLID_ALT_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "textures/gui/sprites/elements/box_solid_alt_atlas.png");

    private final boolean isSolid;
    private final boolean isAlt;
    private int x;
    private int y;
    private final int z;

    private final boolean showTop;
    private final boolean showRight;
    private final boolean showBottom;
    private final boolean showLeft;
    //endregion

    public BoxElement(int x, int y, int width, int height, boolean isSolid, boolean isAlt) {
        super(width,
                height,
                x,
                y,
                null,
                Component.literal("Box Element"),
                false);
        this.isSolid = isSolid;
        this.isAlt = isAlt;
        this.x = x;
        this.y = y;
        this.z = -1;
        this.showTop = true;
        this.showRight = true;
        this.showBottom = true;
        this.showLeft = true;
    }

    public BoxElement(int x, int y, int z, int width, int height, boolean isSolid, boolean isAlt) {
        super(width,
                height,
                x,
                y,
                null,
                Component.literal("Box Element"),
                false);
        this.isSolid = isSolid;
        this.isAlt = isAlt;
        this.x = x;
        this.y = y;
        this.z = z;
        this.showTop = true;
        this.showRight = true;
        this.showBottom = true;
        this.showLeft = true;
    }

    public BoxElement(int x, int y, int z, int width, int height, boolean isSolid, boolean isAlt, boolean showTop, boolean showRight, boolean showBottom, boolean showLeft) {
        super(width,
                height,
                x,
                y,
                null,
                Component.literal("Box Element"),
                false);
        this.isSolid = isSolid;
        this.isAlt = isAlt;
        this.x = x;
        this.y = y;
        this.z = z;
        this.showTop = showTop;
        this.showRight = showRight;
        this.showBottom = showBottom;
        this.showLeft = showLeft;
    }

    //region Methods
    @Override
    public void render(GuiGraphics guiGraphics) {
        this.renderBox(guiGraphics);
    }

    private void renderBox(GuiGraphics guiGraphics) {
        Identifier TEXTURE = isAlt ? (isSolid ? BOX_SOLID_ALT_TEXTURE : BOX_ALT_TEXTURE) : (isSolid ? BOX_SOLID_TEXTURE : BOX_TEXTURE);

        // Top Left
        if(showTop && showLeft) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.x, this.y,
                0, 0,
                5, 5,
                5, 5,
                15,15
        );

        // Top
        if(showTop) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.x + 5, this.y,
                5, 0,
                this.width - 10, 5,
                5, 5,
                15, 15
        );

        // Top Right
        if(showTop && showRight) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x + this.width - 5, this.y,
                10, 0,
                5, 5,
                5, 5,
                15, 15
        );

        // Centre Left
        if(showLeft) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.x, this.y + 5,
                0, 5,
                5, this.height - 10,
                5, 5,
                15,15
        );

        // Centre
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.x + 5, this.y + 5,
                5, 5,
                this.width - 10, this.height - 10,
                5, 5,
                15, 15
        );

        // Centre Right
        if(showRight) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x + this.width - 5, this.y + 5,
                10, 5,
                5, this.height - 10,
                5, 5,
                15, 15
        );

        // Bottom Left
        if(showLeft && showBottom) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.x, y + this.height - 5,
                0, 10,
                5, 5,
                5, 5,
                15,15
        );

        // Bottom
        if(showBottom) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.x + 5, y + this.height - 5,
                5, 10,
                this.width - 10, 5,
                5, 5,
                15, 15
        );

        // Bottom Right
        if(showBottom && showRight) guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x + this.width - 5, y + this.height - 5,
                10, 10,
                5, 5,
                5, 5,
                15, 15
        );
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.width = width;
        this.height = height;
    }
    //endregion
}
