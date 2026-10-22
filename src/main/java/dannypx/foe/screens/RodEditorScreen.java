package dannypx.foe.screens;

import dannypx.foe.config.Configs;
import dannypx.foe.nearby_dispatcher.types.RodType;
import dannypx.foe.screens.element.BoxElement;
import dannypx.foe.screens.element.Element;
import dannypx.foe.screens.widget.SmallButtonBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RodEditorScreen extends DefaultModScreen {
    //region Fields
    private static int PREVIEW_SCALE = 170;
    private static final float HEAD_FOCUS = 0.4f;

    private static final int EDIT_BUTTON_WIDTH = 24;
    private static final int EDIT_BUTTON_HEIGHT = 20;
    private static final int BOX_WIDTH = 40;

    private static final float STEP_SMALL = 0.01f;
    private static final float STEP_BIG = 0.1f;

    private static final float SCALE_VALUE_MIN = 0.50f;
    private static final float SCALE_VALUE_MAX = 1.50f;
    private static final float OFFSET_VALUE_MIN = -0.50f;
    private static final float OFFSET_VALUE_MAX = 0.50f;

    private static final float HAND_OUTWARD = 0.35f;
    private static final float HAND_FORWARD = 0.8f;
    private static final float HAND_DOWN = 0.45f;
    private static final float CROUCH_DROP = 0.1875f;

    private static final float TIP_OUTWARD = 0.0f;
    private static final float TIP_UP = 0.37f;
    private static final float TIP_FORWARD = 0.6f;

    private static final float CUBE_HALF = 0.02f;

    private static final float AVATAR_MODEL_SCALE = 0.9375f;
    private static final float MODEL_ORIGIN_HEIGHT = 1.501f;

    public record CubeBounds(float centerX, float centerY, float centerZ, float halfEdge) {}

    private record LocalPoint(float right, float forward, float up) {}

    private List<Element> elements = new ArrayList<>();

    private EditBox scaleEditBox;
    private EditBox offsetXEditBox;
    private EditBox offsetYEditBox;
    private EditBox offsetZEditBox;
    private EditBox lineOffsetXEditBox;
    private EditBox lineOffsetYEditBox;
    private EditBox lineOffsetZEditBox;
    //endregion

    //region Methods
    public RodEditorScreen(Screen parent) {
        super(parent, Component.literal("Rod Position Editor"));

        if (minecraft.player != null) {
            Inventory inventory = minecraft.player.getInventory();
            inventory.setSelectedSlot(0);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.initWidgets();
        this.initElements();
    }

    @Override
    public void tick() {
        super.tick();
        this.snapIfOutOfRangeScale(scaleEditBox);
        this.snapIfOutOfRangeOffset(offsetXEditBox);
        this.snapIfOutOfRangeOffset(offsetYEditBox);
        this.snapIfOutOfRangeOffset(offsetZEditBox);
        this.snapIfOutOfRangeOffset(lineOffsetXEditBox);
        this.snapIfOutOfRangeOffset(lineOffsetYEditBox);
        this.snapIfOutOfRangeOffset(lineOffsetZEditBox);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, tickDelta);

        if (minecraft.player != null) {
            int originalTickCount = minecraft.player.tickCount;
            minecraft.player.tickCount = 0;
            try {
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        guiGraphics, 0, 0, width, height, PREVIEW_SCALE, HEAD_FOCUS, mouseX, mouseY, minecraft.player);
            } finally {
                minecraft.player.tickCount = originalTickCount;
            }
        }

        this.renderBoxes(guiGraphics);
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.nextStratum();

        this.renderTitles(guiGraphics);
        this.renderHints(guiGraphics);
    }

    private void renderBoxes(GuiGraphics guiGraphics) {
        elements.forEach(element -> element.render(guiGraphics));
    }

    private void renderTitles(GuiGraphics guiGraphics) {
        int topButtonY = offsetBoxY() - (EDIT_BUTTON_HEIGHT + PADDING_QUART) * 2;
        int axisY = topButtonY - font.lineHeight - PADDING_QUART;
        int titleY = axisY - font.lineHeight - PADDING_QUART;

        String[] axes = {"X", "Y", "Z"};
        for (int column = 0; column < 3; column++) {
            Component axis = Component.literal(axes[column]);
            guiGraphics.drawCenteredString(font, axis, offsetBoxX(column) + BOX_WIDTH / 2, axisY, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(font, axis, lineOffsetBoxX(column) + BOX_WIDTH / 2, axisY, 0xFFFFFFFF);
        }

        int rodCenterX = (offsetBoxX(0) + offsetBoxX(2) + BOX_WIDTH) / 2;
        guiGraphics.drawCenteredString(font, Component.literal("Rod offset"), rodCenterX, titleY, 0xFFFFFFFF);

        int lineCenterX = (lineOffsetBoxX(2) + lineOffsetBoxX(0) + BOX_WIDTH) / 2;
        guiGraphics.drawCenteredString(font, Component.literal("Line offset"), lineCenterX, titleY, 0xFFFFFFFF);

        int scaleTitleY = scaleBoxY() - font.lineHeight - PADDING_QUART;
        guiGraphics.drawCenteredString(font, Component.literal("Scale"), scaleBoxX() + BOX_WIDTH / 2, scaleTitleY, 0xFFFFFFFF);
    }

    private void renderHints(GuiGraphics guiGraphics) {
        guiGraphics.drawString(font,
                "\uDB80\uDC67 Scroll to zoom",
                PADDING, minecraft.getWindow().getGuiScaledHeight() - PADDING - font.lineHeight,
                CommonColors.WHITE,
                true
        );
    }

    private LocalPoint lineOriginLocalPoint(Player player) {
        float playerScale = player.getScale();

        HumanoidArm holdingArm = player.getMainHandItem().is(Items.FISHING_ROD) ? player.getMainArm() : player.getMainArm().getOpposite();
        float side = holdingArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;

        float scaleChange = boxValue(scaleEditBox, 1.0f) - 1.0f;
        float outward = boxValue(offsetXEditBox, 0f) + boxValue(lineOffsetXEditBox, 0f) + TIP_OUTWARD * scaleChange;
        float up = boxValue(offsetYEditBox, 0f) + boxValue(lineOffsetYEditBox, 0f) + TIP_UP * scaleChange;
        float forward = boxValue(offsetZEditBox, 0f) + boxValue(lineOffsetZEditBox, 0f) + TIP_FORWARD * scaleChange;

        float localRight = side * (HAND_OUTWARD + outward) * playerScale;
        float localForward = (HAND_FORWARD + forward) * playerScale;
        float localUp = player.getEyeHeight() - (player.isCrouching() ? CROUCH_DROP : 0f) - HAND_DOWN * playerScale + up * playerScale;
        return new LocalPoint(localRight, localForward, localUp);
    }

    public CubeBounds cubeInModelSpace(Player player) {
        LocalPoint origin = lineOriginLocalPoint(player);
        float worldToModel = 1.0f / (player.getScale() * AVATAR_MODEL_SCALE);
        float worldHalfEdge = CUBE_HALF * player.getScale() * boxValue(scaleEditBox, 1.0f);

        return new CubeBounds(
                -origin.right() * worldToModel,
                MODEL_ORIGIN_HEIGHT - origin.up() * worldToModel,
                -origin.forward() * worldToModel,
                worldHalfEdge * worldToModel
        );
    }

    private float boxValue(EditBox box, float fallback) {
        if (box == null) return fallback;
        Float value = parseFloat(box.getValue());
        return value != null ? value : fallback;
    }

    private void initWidgets() {
        List<AbstractWidget> widgets = new ArrayList<>();

        widgets.add(this.getScaleEditBox());
        widgets.add(this.getOffsetXEditBox());
        widgets.add(this.getOffsetYEditBox());
        widgets.add(this.getOffsetZEditBox());
        widgets.add(this.getLineOffsetXEditBox());
        widgets.add(this.getLineOffsetYEditBox());
        widgets.add(this.getLineOffsetZEditBox());

        widgets.add(this.getScaleButton("--", scaleBoxX() - (PADDING_QUART + EDIT_BUTTON_WIDTH) * 2, -STEP_BIG, Tooltip.create(Component.literal("Decrease scale by 0.1"))));
        widgets.add(this.getScaleButton("-", scaleBoxX() - (PADDING_QUART + EDIT_BUTTON_WIDTH), -STEP_SMALL, Tooltip.create(Component.literal("Decrease scale by 0.01"))));
        widgets.add(this.getScaleButton("+", scaleBoxX() + BOX_WIDTH + PADDING_QUART, STEP_SMALL, Tooltip.create(Component.literal("Increase scale by 0.01"))));
        widgets.add(this.getScaleButton("++", scaleBoxX() + BOX_WIDTH + (PADDING_QUART + EDIT_BUTTON_WIDTH) + PADDING_QUART, STEP_BIG, Tooltip.create(Component.literal("Increase scale by 0.1"))));

        int rowStep = EDIT_BUTTON_HEIGHT + PADDING_QUART;
        widgets.add(this.getOffsetButton("--", offsetBoxY() + rowStep * 2, 0, -STEP_BIG, offsetXEditBox, Tooltip.create(Component.literal("Decrease offset by 0.1"))));
        widgets.add(this.getOffsetButton("-", offsetBoxY() + rowStep, 0, -STEP_SMALL, offsetXEditBox, Tooltip.create(Component.literal("Decrease offset by 0.01"))));
        widgets.add(this.getOffsetButton("+", offsetBoxY() - rowStep, 0, STEP_SMALL, offsetXEditBox, Tooltip.create(Component.literal("Increase offset by 0.01"))));
        widgets.add(this.getOffsetButton("++", offsetBoxY() - rowStep * 2, 0, STEP_BIG, offsetXEditBox, Tooltip.create(Component.literal("Increase offset by 0.1"))));

        widgets.add(this.getOffsetButton("--", offsetBoxY() + rowStep * 2, 1, -STEP_BIG, offsetYEditBox, Tooltip.create(Component.literal("Decrease offset by 0.1"))));
        widgets.add(this.getOffsetButton("-", offsetBoxY() + rowStep, 1, -STEP_SMALL, offsetYEditBox, Tooltip.create(Component.literal("Decrease offset by 0.01"))));
        widgets.add(this.getOffsetButton("+", offsetBoxY() - rowStep, 1, STEP_SMALL, offsetYEditBox, Tooltip.create(Component.literal("Increase offset by 0.01"))));
        widgets.add(this.getOffsetButton("++", offsetBoxY() - rowStep * 2, 1, STEP_BIG, offsetYEditBox, Tooltip.create(Component.literal("Increase offset by 0.1"))));

        widgets.add(this.getOffsetButton("--", offsetBoxY() + rowStep * 2, 2, -STEP_BIG, offsetZEditBox, Tooltip.create(Component.literal("Decrease offset by 0.1"))));
        widgets.add(this.getOffsetButton("-", offsetBoxY() + rowStep, 2, -STEP_SMALL, offsetZEditBox, Tooltip.create(Component.literal("Decrease offset by 0.01"))));
        widgets.add(this.getOffsetButton("+", offsetBoxY() - rowStep, 2, STEP_SMALL, offsetZEditBox, Tooltip.create(Component.literal("Increase offset by 0.01"))));
        widgets.add(this.getOffsetButton("++", offsetBoxY() - rowStep * 2, 2, STEP_BIG, offsetZEditBox, Tooltip.create(Component.literal("Increase offset by 0.1"))));

        widgets.add(this.getLineOffsetButton("--", offsetBoxY() + rowStep * 2, 0, -STEP_BIG, lineOffsetXEditBox, Tooltip.create(Component.literal("Decrease offset by 0.1"))));
        widgets.add(this.getLineOffsetButton("-", offsetBoxY() + rowStep, 0, -STEP_SMALL, lineOffsetXEditBox, Tooltip.create(Component.literal("Decrease offset by 0.01"))));
        widgets.add(this.getLineOffsetButton("+", offsetBoxY() - rowStep, 0, STEP_SMALL, lineOffsetXEditBox, Tooltip.create(Component.literal("Increase offset by 0.01"))));
        widgets.add(this.getLineOffsetButton("++", offsetBoxY() - rowStep * 2, 0, STEP_BIG, lineOffsetXEditBox, Tooltip.create(Component.literal("Increase offset by 0.1"))));

        widgets.add(this.getLineOffsetButton("--", offsetBoxY() + rowStep * 2, 1, -STEP_BIG, lineOffsetYEditBox, Tooltip.create(Component.literal("Decrease offset by 0.1"))));
        widgets.add(this.getLineOffsetButton("-", offsetBoxY() + rowStep, 1, -STEP_SMALL, lineOffsetYEditBox, Tooltip.create(Component.literal("Decrease offset by 0.01"))));
        widgets.add(this.getLineOffsetButton("+", offsetBoxY() - rowStep, 1, STEP_SMALL, lineOffsetYEditBox, Tooltip.create(Component.literal("Increase offset by 0.01"))));
        widgets.add(this.getLineOffsetButton("++", offsetBoxY() - rowStep * 2, 1, STEP_BIG, lineOffsetYEditBox, Tooltip.create(Component.literal("Increase offset by 0.1"))));

        widgets.add(this.getLineOffsetButton("--", offsetBoxY() + rowStep * 2, 2, -STEP_BIG, lineOffsetZEditBox, Tooltip.create(Component.literal("Decrease offset by 0.1"))));
        widgets.add(this.getLineOffsetButton("-", offsetBoxY() + rowStep, 2, -STEP_SMALL, lineOffsetZEditBox, Tooltip.create(Component.literal("Decrease offset by 0.01"))));
        widgets.add(this.getLineOffsetButton("+", offsetBoxY() - rowStep, 2, STEP_SMALL, lineOffsetZEditBox, Tooltip.create(Component.literal("Increase offset by 0.01"))));
        widgets.add(this.getLineOffsetButton("++", offsetBoxY() - rowStep * 2, 2, STEP_BIG, lineOffsetZEditBox, Tooltip.create(Component.literal("Increase offset by 0.1"))));

        widgets.forEach(this::addRenderableWidget);
    }

    private void initElements() {
        this.elements.clear();

        int topButtonY = offsetBoxY() - (EDIT_BUTTON_HEIGHT + PADDING_QUART) * 2;
        int axisY = topButtonY - font.lineHeight - PADDING_QUART;
        int titleY = axisY - font.lineHeight - PADDING_QUART;

        this.elements.add(
                new BoxElement(
                        - 5, titleY - PADDING_QUART - 4,
                        BOX_WIDTH * 3 + PADDING_HALF * 3 + PADDING_QUART + 8, EDIT_BUTTON_HEIGHT * 5 + font.lineHeight * 2 + PADDING_QUART * 8 + 8,
                        false, false
                )
        );

        this.elements.add(
                new BoxElement(
                        width + 3 - (BOX_WIDTH * 3 + PADDING_HALF * 3 + PADDING_QUART + 8), titleY - PADDING_QUART - 4,
                        BOX_WIDTH * 3 + PADDING_HALF * 3 + PADDING_QUART * 2 + 8, EDIT_BUTTON_HEIGHT * 5 + font.lineHeight * 2 + PADDING_QUART * 8 + 8,
                        false, false
                )
        );

        this.elements.add(
                new BoxElement(
                        scaleBoxX() - (PADDING_QUART + EDIT_BUTTON_WIDTH) * 2 - PADDING_HALF, scaleBoxY() - PADDING_HALF * 2 - font.lineHeight,
                        EDIT_BUTTON_WIDTH * 4 + BOX_WIDTH + PADDING_QUART * 3 + PADDING_HALF * 3, PADDING_HALF * 2 + EDIT_BUTTON_HEIGHT + PADDING + font.lineHeight,
                        false, false
                )
        );
    }

    private int scaleBoxX() {
        return width / 2 - BOX_WIDTH / 2;
    }

    private int scaleBoxY() {
        return height - EDIT_BUTTON_HEIGHT - PADDING_HALF;
    }

    private int offsetBoxX(int column) {
        return PADDING_HALF + (BOX_WIDTH + PADDING_HALF) * column;
    }

    private int offsetBoxY() {
        return height / 2 - EDIT_BUTTON_HEIGHT / 2;
    }

    private int lineOffsetBoxX(int column) {
        return width - (BOX_WIDTH + PADDING_HALF) * (1 + column);
    }

    private EditBox getScaleEditBox() {
        scaleEditBox = new EditBox(font, scaleBoxX(), scaleBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Scale"));
        scaleEditBox.setFilter(text -> text.matches("[0-9]*\\.?[0-9]*"));
        scaleEditBox.setValue(format(Configs.rendererConfig.rodScale.get()));
        scaleEditBox.setResponder(this::onScaleChanged);
        scaleEditBox.setTooltip(Tooltip.create(Component.literal("Scale")));
        return scaleEditBox;
    }

    private EditBox getOffsetXEditBox() {
        offsetXEditBox = new EditBox(font, offsetBoxX(0), offsetBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Rod Offset X"));
        offsetXEditBox.setFilter(text -> text.matches("-?[0-9]*\\.?[0-9]*"));
        offsetXEditBox.setValue(format(Configs.rendererConfig.rodXPosition.get()));
        offsetXEditBox.setResponder(this::onOffsetXChanged);
        offsetXEditBox.setTooltip(Tooltip.create(Component.literal("Rod Offset X")));
        return offsetXEditBox;
    }

    private EditBox getOffsetYEditBox() {
        offsetYEditBox = new EditBox(font, offsetBoxX(1), offsetBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Rod Offset Y"));
        offsetYEditBox.setFilter(text -> text.matches("-?[0-9]*\\.?[0-9]*"));
        offsetYEditBox.setValue(format(Configs.rendererConfig.rodYPosition.get()));
        offsetYEditBox.setResponder(this::onOffsetYChanged);
        offsetYEditBox.setTooltip(Tooltip.create(Component.literal("Rod Offset Y")));
        return offsetYEditBox;
    }

    private EditBox getOffsetZEditBox() {
        offsetZEditBox = new EditBox(font, offsetBoxX(2), offsetBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Rod Offset Z"));
        offsetZEditBox.setFilter(text -> text.matches("-?[0-9]*\\.?[0-9]*"));
        offsetZEditBox.setValue(format(Configs.rendererConfig.rodZPosition.get()));
        offsetZEditBox.setResponder(this::onOffsetZChanged);
        offsetZEditBox.setTooltip(Tooltip.create(Component.literal("Rod Offset Z")));
        return offsetZEditBox;
    }

    private EditBox getLineOffsetXEditBox() {
        lineOffsetXEditBox = new EditBox(font, lineOffsetBoxX(0), offsetBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Line Offset X"));
        lineOffsetXEditBox.setFilter(text -> text.matches("-?[0-9]*\\.?[0-9]*"));
        lineOffsetXEditBox.setValue(format(Configs.rendererConfig.lineXPosition.get()));
        lineOffsetXEditBox.setResponder(this::onLineOffsetXChanged);
        lineOffsetXEditBox.setTooltip(Tooltip.create(Component.literal("Line Offset X")));
        return lineOffsetXEditBox;
    }

    private EditBox getLineOffsetYEditBox() {
        lineOffsetYEditBox = new EditBox(font, lineOffsetBoxX(1), offsetBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Line Offset Y"));
        lineOffsetYEditBox.setFilter(text -> text.matches("-?[0-9]*\\.?[0-9]*"));
        lineOffsetYEditBox.setValue(format(Configs.rendererConfig.lineYPosition.get()));
        lineOffsetYEditBox.setResponder(this::onLineOffsetYChanged);
        lineOffsetYEditBox.setTooltip(Tooltip.create(Component.literal("Line Offset Y")));
        return lineOffsetYEditBox;
    }

    private EditBox getLineOffsetZEditBox() {
        lineOffsetZEditBox = new EditBox(font, lineOffsetBoxX(2), offsetBoxY(), BOX_WIDTH, EDIT_BUTTON_HEIGHT, Component.literal("Line Offset Z"));
        lineOffsetZEditBox.setFilter(text -> text.matches("-?[0-9]*\\.?[0-9]*"));
        lineOffsetZEditBox.setValue(format(Configs.rendererConfig.lineZPosition.get()));
        lineOffsetZEditBox.setResponder(this::onLineOffsetZChanged);
        lineOffsetZEditBox.setTooltip(Tooltip.create(Component.literal("Line Offset Z")));
        return lineOffsetZEditBox;
    }

    private AbstractWidget getScaleButton(String label, int x, float step, Tooltip tooltip) {
        return new SmallButtonBuilder(
                x + PADDING_QUART, scaleBoxY() + PADDING_QUART,
                EDIT_BUTTON_WIDTH - PADDING_QUART, EDIT_BUTTON_HEIGHT - PADDING_QUART,
                button -> adjustScale(scaleEditBox, step))
                .icon(label)
                .color(step < 0 ? 0xFFCC0000 : 0xFF00CA3A)
                .tooltip(tooltip)
                .build();
    }

    private AbstractWidget getOffsetButton(String label, int y, int column, float step, EditBox editBox, Tooltip tooltip) {
        return new SmallButtonBuilder(
                offsetBoxX(column) + PADDING_QUART, y + PADDING_QUART,
                BOX_WIDTH - PADDING_QUART, EDIT_BUTTON_HEIGHT - PADDING_QUART,
                button -> adjustOffset(editBox, step))
                .icon(label)
                .color(step < 0 ? 0xFFCC0000 : 0xFF00CA3A)
                .tooltip(tooltip)
                .build();
    }

    private AbstractWidget getLineOffsetButton(String label, int y, int column, float step, EditBox editBox, Tooltip tooltip) {
        return new SmallButtonBuilder(
                lineOffsetBoxX(column) + PADDING_QUART, y + PADDING_QUART,
                BOX_WIDTH - PADDING_QUART, EDIT_BUTTON_HEIGHT - PADDING_QUART,
                button -> adjustOffset(editBox, step))
                .icon(label)
                .color(step < 0 ? 0xFFCC0000 : 0xFF00CA3A)
                .tooltip(tooltip)
                .build();
    }

    private void onScaleChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, SCALE_VALUE_MIN, SCALE_VALUE_MAX);

        Configs.rendererConfig.rodScale.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void onOffsetXChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX);

        Configs.rendererConfig.rodXPosition.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void onOffsetYChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX);

        Configs.rendererConfig.rodYPosition.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void onOffsetZChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX);

        Configs.rendererConfig.rodZPosition.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void onLineOffsetXChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX);

        Configs.rendererConfig.lineXPosition.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void onLineOffsetYChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX);

        Configs.rendererConfig.lineYPosition.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void onLineOffsetZChanged(String text) {
        Float value = parseFloat(text);
        if (value == null) return;
        float clamped = Math.clamp(value, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX);

        Configs.rendererConfig.lineZPosition.accept(clamped);
        Configs.rendererConfig.save();
    }

    private void adjustScale(EditBox box, float delta) {
        Float current = parseFloat(box.getValue());
        float base = current != null ? current : 1.0f;
        float next = Math.clamp(Math.round((base + delta) * 100.0f) / 100.0f, SCALE_VALUE_MIN, SCALE_VALUE_MAX);
        box.setValue(format(next));
    }

    private void adjustOffset(EditBox box, float delta) {
        Float current = parseFloat(box.getValue());
        float base = current != null ? current : 0.0f;
        float next = Math.clamp(Math.round((base + delta) * 100.0f) / 100.0f, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX);
        box.setValue(format(next));
    }

    private static Float parseFloat(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void snapIfOutOfRangeScale(EditBox box) {
        if (box == null || box.isFocused()) return;
        Float value = parseFloat(box.getValue());
        float target = value != null ? Math.clamp(value, SCALE_VALUE_MIN, SCALE_VALUE_MAX) : 1.0f;
        if (value == null || value != target) {
            box.setValue(format(target));
        }
    }

    private void snapIfOutOfRangeOffset(EditBox box) {
        if (box == null || box.isFocused()) return;
        Float value = parseFloat(box.getValue());
        float target = value != null ? Math.clamp(value, OFFSET_VALUE_MIN, OFFSET_VALUE_MAX) : 0.0f;
        if (value == null || value != target) {
            box.setValue(format(target));
        }
    }

    private static String format(float value) {
        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(verticalAmount > 0 && PREVIEW_SCALE > 0) PREVIEW_SCALE += 3;
        else if(PREVIEW_SCALE > 0) PREVIEW_SCALE -= 3;

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        RodType.reload();
        super.onClose();
    }

    //endregion
}