package dannypx.foe.screens;

import dannypx.foe.config.Configs;
import dannypx.foe.nearby_dispatcher.types.HatType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerHatEditorScreen extends DefaultModScreen {
    //region Fields
    private static final int PREVIEW_SCALE = 190;
    private static final float HEAD_FOCUS = 0.8f;

    private static final int EDIT_BUTTON_WIDTH = 24;
    private static final int EDIT_BUTTON_HEIGHT = 20;
    private static final int BOX_WIDTH = 50;
    private static final int GAP = 2;

    private static final float SCALE_STEP_SMALL = 0.01f;
    private static final float SCALE_STEP_BIG = 0.1f;
    private static final float HEIGHT_STEP_SMALL = 0.01f;
    private static final float HEIGHT_STEP_BIG = 0.1f;

    private static final float VALUE_MIN = 0.50f;
    private static final float VALUE_MAX = 1.50f;

    private EditBox scaleEditBox;
    private EditBox heightEditBox;
    //endregion

    //region Methods
    public PlayerHatEditorScreen(Screen parent) {
        super(parent, Component.literal("Hat Position Editor"));
    }

    @Override
    protected void init() {
        super.init();
        this.renderWidgets();
    }

    @Override
    public void tick() {
        super.tick();
        this.snapIfOutOfRange(scaleEditBox);
        this.snapIfOutOfRange(heightEditBox);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, tickDelta);

        if (minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics, 0, 0, width, height, PREVIEW_SCALE, HEAD_FOCUS, mouseX, mouseY, minecraft.player);
        }
    }

    private void renderWidgets() {
        List<AbstractWidget> widgets = new ArrayList<>();

        widgets.add(this.getScaleEditBox());
        widgets.add(this.getHeightEditBox());

        widgets.add(this.getScaleButton("--", scaleBoxX() - (GAP + EDIT_BUTTON_WIDTH) * 2, -SCALE_STEP_BIG, Tooltip.create(Component.literal("Decrease scale by 0.1"))));
        widgets.add(this.getScaleButton("-", scaleBoxX() - (GAP + EDIT_BUTTON_WIDTH), -SCALE_STEP_SMALL, Tooltip.create(Component.literal("Decrease scale by 0.01"))));
        widgets.add(this.getScaleButton("+", scaleBoxX() + BOX_WIDTH + GAP, SCALE_STEP_SMALL, Tooltip.create(Component.literal("Increase scale by 0.01"))));
        widgets.add(this.getScaleButton("++", scaleBoxX() + BOX_WIDTH + (GAP + EDIT_BUTTON_WIDTH) + GAP, SCALE_STEP_BIG, Tooltip.create(Component.literal("Increase scale by 0.1"))));

        int rowStep = EDIT_BUTTON_HEIGHT + GAP;
        widgets.add(this.getHeightButton("--", heightBoxY() + rowStep * 2, -HEIGHT_STEP_BIG, Tooltip.create(Component.literal("Decrease height by 0.1"))));
        widgets.add(this.getHeightButton("-", heightBoxY() + rowStep, -HEIGHT_STEP_SMALL, Tooltip.create(Component.literal("Decrease height by 0.01"))));
        widgets.add(this.getHeightButton("+", heightBoxY() - rowStep, HEIGHT_STEP_SMALL, Tooltip.create(Component.literal("Increase height by 0.01"))));
        widgets.add(this.getHeightButton("++", heightBoxY() - rowStep * 2, HEIGHT_STEP_BIG, Tooltip.create(Component.literal("Increase height by 0.1"))));

        widgets.forEach(this::addRenderableWidget);
    }

    private int scaleBoxX() {
        return width / 2 - BOX_WIDTH / 2;
    }

    private int scaleBoxY() {
        return height - EDIT_BUTTON_HEIGHT - PADDING_HALF;
    }

    private int heightBoxX() {
        return width - BOX_WIDTH - PADDING_HALF;
    }

    private int heightBoxY() {
        return height / 2 - EDIT_BUTTON_HEIGHT / 2;
    }

    private EditBox getScaleEditBox() {
        scaleEditBox = new EditBox(font, scaleBoxX(), scaleBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Scale"));
        scaleEditBox.setFilter(text -> text.matches("[0-9]*\\.?[0-9]*"));
        scaleEditBox.setValue(format(Configs.rendererConfig.hatScale.get()));
        scaleEditBox.setResponder(this::onScaleChanged);
        scaleEditBox.setTooltip(Tooltip.create(Component.literal("Scale")));
        return scaleEditBox;
    }

    private EditBox getHeightEditBox() {
        heightEditBox = new EditBox(font, heightBoxX(), heightBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Height"));
        heightEditBox.setFilter(text -> text.matches("-?[0-9]*\\.?[0-9]*"));
        heightEditBox.setValue(format(Configs.rendererConfig.hatHeight.get()));
        heightEditBox.setResponder(this::onHeightChanged);
        heightEditBox.setTooltip(Tooltip.create(Component.literal("Height")));
        return heightEditBox;
    }

    private Button getScaleButton(String label, int x, float step, Tooltip tooltip) {
        return Button.builder(Component.literal(label), button -> adjust(scaleEditBox, step))
                .bounds(x, scaleBoxY(), EDIT_BUTTON_WIDTH, EDIT_BUTTON_HEIGHT)
                .tooltip(tooltip)
                .build();
    }

    private Button getHeightButton(String label, int y, float step, Tooltip tooltip) {
        return Button.builder(Component.literal(label), button -> adjust(heightEditBox, step))
                .bounds(heightBoxX(), y, BOX_WIDTH, EDIT_BUTTON_HEIGHT)
                .tooltip(tooltip)
                .build();
    }

    private void onScaleChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, VALUE_MIN, VALUE_MAX);

        Configs.rendererConfig.hatScale.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void onHeightChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, VALUE_MIN, VALUE_MAX);

        Configs.rendererConfig.hatHeight.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void adjust(EditBox box, float delta) {
        Float current = parseFloat(box.getValue());
        float base = current != null ? current : 1.0f;
        float next = Math.clamp(Math.round((base + delta) * 100.0f) / 100.0f, VALUE_MIN, VALUE_MAX);
        box.setValue(format(next));
    }

    private static Float parseFloat(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void snapIfOutOfRange(EditBox box) {
        if (box == null || box.isFocused()) return;
        Float value = parseFloat(box.getValue());
        float target = value != null ? Math.clamp(value, VALUE_MIN, VALUE_MAX) : 1.0f;
        if (value == null || value != target) {
            box.setValue(format(target));
        }
    }

    private static String format(float value) {
        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    public void onClose() {
        HatType.reload();
        super.onClose();
    }

    //endregion
}
