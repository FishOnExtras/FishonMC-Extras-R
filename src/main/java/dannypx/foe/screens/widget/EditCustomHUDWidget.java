package dannypx.foe.screens.widget;

import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.store.CustomHudDataHandler;
import dannypx.foe.screens.interfaces.ScreenConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditCustomHUDWidget extends ClickableWidget implements ScreenConstants {
    MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private final List<LineEntry> entries = new ArrayList<>();
    private LineEntry focusedEntry = null;

    private Text header;
    private final int headerHeight = 20;

    private final int textFieldHeight = 20;

    private final TextFieldWidget idTextField;
    public String newName;

    private final TextFieldWidget scaleTextField;
    public float scale;

    private final CheckboxWidget showBackgroundCheckBox;
    public boolean showBackground;

    private final CheckboxWidget showElementCheckBox;
    public boolean showElement;

    public boolean hasSelectedOption = false;
    public String currentSelectedHud = null;

    private int scrollOffset = 0;
    private final int scrollbarWidth = 6;

    public EditCustomHUDWidget(int x, int y, int width, int height, Text header) {
        super(x, y, width, height, Text.empty());
        this.header = header;
        idTextField = new TextFieldWidget(
                minecraftClient.textRenderer,
                getX() + PADDING,
                getY() + headerHeight + PADDING,
                width / 3 - PADDING - PADDING_HALF,
                textFieldHeight,
                Text.empty()
        );
        idTextField.setMaxLength(Integer.MAX_VALUE);

        newName = "";

        idTextField.setChangedListener(s -> {
            if (hasSelectedOption) {
                newName = s;
            }
            idTextField.setPlaceholder(Text.literal(s));
        });

        idTextField.setText("");

        scaleTextField = new TextFieldWidget(
                minecraftClient.textRenderer,
                getX() + width / 3 + minecraftClient.textRenderer.getWidth("Scale") + PADDING_HALF,
                getY() + headerHeight + PADDING,
                40,
                textFieldHeight,
                Text.empty()
        );
        scaleTextField.setMaxLength(5);

        scale = 1.0f;

        scaleTextField.setChangedListener(s -> {
            if (hasSelectedOption) {
                try {
                    scale = Float.parseFloat(s);
                } catch (Exception e) {
                    scale = 1.0f;
                }
            }
            scaleTextField.setPlaceholder(Text.literal(s));
        });

        scaleTextField.setText("");

        showBackgroundCheckBox = CheckboxWidget.builder(Text.literal("Show Background"), minecraftClient.textRenderer)
                .pos(
                        getX() + width / 3 + minecraftClient.textRenderer.getWidth("Scale") + PADDING_HALF + 40 + PADDING_HALF,
                        getY() + headerHeight + PADDING
                )
                .checked(true)
                .callback((checkbox, checked) -> showBackground = checked)
                .build();
        showBackground = true;

        showElementCheckBox = CheckboxWidget.builder(Text.literal("Show Element"), minecraftClient.textRenderer)
                .pos(
                        getX() + width / 3 + minecraftClient.textRenderer.getWidth("Scale") + PADDING_HALF + 40 + PADDING_HALF
                                + minecraftClient.textRenderer.getWidth("Show Background") + PADDING_HALF + 16 + PADDING_HALF,
                        getY() + headerHeight + PADDING
                )
                .checked(true)
                .callback((checkbox, checked) -> showElement = checked)
                .build();
        showElement = true;
    }

    public List<LineEntry> getEntries() {
        return entries;
    }

    public void selectHud(String id, CustomHudDataHandler.CustomHud customHud) {
        removeAllEntries();
        hasSelectedOption = true;
        newName = id;
        scale = customHud.scale;
        currentSelectedHud = id;
        header = Text.literal(id);
        idTextField.setText(id);
        idTextField.setPlaceholder(Text.literal(id));
        scaleTextField.setText(String.format(Locale.US, "%f", customHud.scale));
        scaleTextField.setPlaceholder(Text.literal(String.format(Locale.US, "%f", customHud.scale)));
        showBackground = customHud.showBackground;
        if(customHud.showBackground != showBackgroundCheckBox.isChecked()) {
            showBackgroundCheckBox.onPress();
        }
        showElement = customHud.showElement;
        if(customHud.showElement != showElementCheckBox.isChecked()) {
            showElementCheckBox.onPress();
        }

        customHud.textLines.forEach(line -> this.addEntry(new LineEntry(
                line.value1(),
                line.value2(),
                line.value3(),
                width,
                getDefaultCallback()
        )));
    }

    public void addEntry(LineEntry entry) {
        entries.add(entry);
    }

    public void addEntry(int pos, LineEntry entry) {
        entries.add(pos, entry);
    }

    public void addNewEntry() {
        this.addEntry(getDefaultEntry());
    }

    public void addNewEntry(int pos) {
        this.addEntry(pos, getDefaultEntry());
    }

    private LineEntry getDefaultEntry() {
        return new EditCustomHUDWidget.LineEntry(
                "Example text",
                false,
                false,
                width,
                getDefaultCallback()
        );
    }

    private LineEntry.Callback getDefaultCallback() {
        return new LineEntry.Callback() {
            @Override
            public void onDelete(LineEntry lineEntry) {
                CodeExecuterHandler.runLater(1, "EditCustomHUDWidget > getDefaultCallback > onDelete", () -> removeEntry(lineEntry));
            }

            @Override
            public void onAdd(LineEntry lineEntry) {
                CodeExecuterHandler.runLater(1, "EditCustomHUDWidget > getDefaultCallback > onDelete", () -> addNewEntry(entries.indexOf(lineEntry)));
            }
        };
    }

    public void removeEntry(LineEntry entry) {
        entries.remove(entry);
    }

    public void removeAllEntries() {
        entries.clear();
    }

    public void reset() {
        this.removeAllEntries();
        hasSelectedOption = false;
        newName = "";
        scale = 1.0f;
        currentSelectedHud = null;
        showBackground = true;
        if(!showBackgroundCheckBox.isChecked()) {
            showBackgroundCheckBox.onPress();
        }
        showElement = true;
        if(!showElementCheckBox.isChecked()) {
            showElementCheckBox.onPress();
        }
        scaleTextField.setText("1.0");
        scaleTextField.setPlaceholder(Text.literal("1.0"));
        idTextField.setText("");
        idTextField.setPlaceholder(Text.literal(""));
        header = Text.literal("No Hud Selected");

    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int entryStartY = getY() + headerHeight + PADDING + textFieldHeight + PADDING;

        context.fill(getX(), getY(), getRight(), getBottom(), 0x55000000);
        context.drawHorizontalLine(getX(), getRight(), getBottom(), 0xFF747474);
        context.drawVerticalLine(getX(), 0, getBottom(), 0xFF747474);
        context.drawCenteredTextWithShadow(
                minecraftClient.textRenderer,
                header,
                getX() + width / 2,
                getY() + PADDING,
                0xFFFFFF
        );

        // Draw scale text
        context.drawText(
                minecraftClient.textRenderer,
                "Scale",
                getX() + width / 3,
                getY() + PADDING + headerHeight + headerHeight / 2 - minecraftClient.textRenderer.fontHeight / 2,
                0xFFFFFF,
                true
        );

        idTextField.render(context, mouseX, mouseY, delta);
        scaleTextField.render(context, mouseX, mouseY, delta);
        showBackgroundCheckBox.render(context, mouseX, mouseY, delta);
        showElementCheckBox.render(context, mouseX, mouseY, delta);

        context.enableScissor(
                getX() + PADDING,
                entryStartY,
                getRight() - PADDING,
                getBottom() - PADDING
        );

        int startY = entryStartY - scrollOffset;

        for (int i = 0; i < entries.size(); i++) {
            int entryY = startY + i * LineEntry.HEIGHT;
            if (entryY + LineEntry.HEIGHT < entryStartY || entryY > getBottom() - PADDING)
                continue;

            LineEntry entry = entries.get(i);
            entry.setPosition(getX() + PADDING, entryY, width - PADDING - PADDING - scrollbarWidth - PADDING);
            entry.render(context, mouseX, mouseY, delta);
        }

        int totalContentHeight = entries.size() * LineEntry.HEIGHT;
        int visibleHeight = height - PADDING - PADDING - headerHeight - textFieldHeight - PADDING * 2;

        if (totalContentHeight > visibleHeight) {
            int scrollbarHeight = Math.max(10, visibleHeight * visibleHeight / totalContentHeight);

            int scrollbarY = entryStartY + scrollOffset * visibleHeight / totalContentHeight;

            int scrollbarX = getX() + width - PADDING - scrollbarWidth;

            context.fill(
                    scrollbarX,
                    scrollbarY,
                    scrollbarX + scrollbarWidth,
                    scrollbarY + scrollbarHeight,
                    0xFFAAAAAA
            );
        }

        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) {
            if (focusedEntry != null) {
                focusedEntry.setFocused(false);
                focusedEntry = null;
            }
            return false;
        }

        if (idTextField.mouseClicked(mouseX, mouseY, button)) {
            if (focusedEntry != null) {
                focusedEntry.setFocused(false);
                focusedEntry = null;
            }
            showBackgroundCheckBox.setFocused(false);
            showElementCheckBox.setFocused(false);
            idTextField.setFocused(true);
            scaleTextField.setFocused(false);
            return true;
        }

        if (scaleTextField.mouseClicked(mouseX, mouseY, button)) {
            if (focusedEntry != null) {
                focusedEntry.setFocused(false);
                focusedEntry = null;
            }
            showBackgroundCheckBox.setFocused(false);
            showElementCheckBox.setFocused(false);
            scaleTextField.setFocused(true);
            idTextField.setFocused(false);
            return true;
        }

        if(showBackgroundCheckBox.mouseClicked(mouseX, mouseY, button)) {
            if (focusedEntry != null) {
                focusedEntry.setFocused(false);
                focusedEntry = null;
            }
            showBackgroundCheckBox.setFocused(true);
            showElementCheckBox.setFocused(false);
            scaleTextField.setFocused(false);
            idTextField.setFocused(false);
            return true;
        }

        if(showElementCheckBox.mouseClicked(mouseX, mouseY, button)) {
            if (focusedEntry != null) {
                focusedEntry.setFocused(false);
                focusedEntry = null;
            }
            showBackgroundCheckBox.setFocused(false);
            showElementCheckBox.setFocused(true);
            scaleTextField.setFocused(false);
            idTextField.setFocused(false);
            return true;
        }

        for (LineEntry entry : entries) {
            if (entry.mouseClicked(mouseX, mouseY, button)) {
                if (focusedEntry != null && focusedEntry != entry) focusedEntry.setFocused(false);
                focusedEntry = entry;
                entry.setFocused(true);
                idTextField.setFocused(false);
                scaleTextField.setFocused(false);
                showBackgroundCheckBox.setFocused(false);
                showElementCheckBox.setFocused(false);
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (idTextField.isFocused()) return idTextField.keyPressed(keyCode, scanCode, modifiers);
        if (scaleTextField.isFocused()) return scaleTextField.keyPressed(keyCode, scanCode, modifiers);
        if (showBackgroundCheckBox.isFocused()) return showBackgroundCheckBox.keyPressed(keyCode, scanCode, modifiers);
        if (showElementCheckBox.isFocused()) return showElementCheckBox.keyPressed(keyCode, scanCode, modifiers);
        if (focusedEntry != null) return focusedEntry.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (idTextField.isFocused()) return idTextField.charTyped(chr, modifiers);
        if (scaleTextField.isFocused()) return scaleTextField.charTyped(chr, modifiers);
        if (showBackgroundCheckBox.isFocused()) return showBackgroundCheckBox.charTyped(chr, modifiers);
        if (showElementCheckBox.isFocused()) return showElementCheckBox.charTyped(chr, modifiers);
        if (focusedEntry != null) return focusedEntry.charTyped(chr, modifiers);
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {

        scrollOffset -= (int) (verticalAmount * 10);

        int visibleHeight = height - PADDING - PADDING - headerHeight - textFieldHeight - PADDING * 2;
        int totalContentHeight = entries.size() * LineEntry.HEIGHT;
        int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);

        return true;
    }

    public static class LineEntry {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        private final TextFieldWidget textFieldWidget;
        private final CheckboxWidget isCentreWidget;
        private final CheckboxWidget isSmallWidget;
        private final ButtonWidget addButton;
        private final ButtonWidget deleteButton;

        public String lineString;
        public boolean isCentre;
        public boolean isSmall;
        public int width;

        private static final String isCentreText = "Centered";
        private static final int CENTRE_TEXT_SPACING = MinecraftClient.getInstance().textRenderer.getWidth(isCentreText);
        private static final String isSmallText = "Small Text";
        private static final int SMALL_TEXT_SPACING = MinecraftClient.getInstance().textRenderer.getWidth(isSmallText);

        public static final int HEIGHT = 24;
        private static final int SPACING = 6;
        private static final int CHECKBOX_SIZE = 20;
        private static final int BUTTON_SIZE = 25;

        public LineEntry(String defaultLine, boolean defaultIsCentre, boolean defaultIsSmall, int width, Callback callback) {
            lineString = defaultLine;
            isCentre = defaultIsCentre;
            isSmall = defaultIsSmall;
            this.width = width;

            textFieldWidget = new TextFieldWidget(
                    minecraftClient.textRenderer,
                    0, 0,
                    0, 20,
                    Text.empty()
            );
            textFieldWidget.setMaxLength(Integer.MAX_VALUE);

            textFieldWidget.setText(defaultLine);
            int textWidth = width - PADDING - PADDING - 6 - PADDING - SPACING - BUTTON_SIZE * 2 - PADDING_QUART - SPACING - SMALL_TEXT_SPACING - CHECKBOX_SIZE - SPACING - CHECKBOX_SIZE - CENTRE_TEXT_SPACING - 20;
            textFieldWidget.setPlaceholder(Text.literal(
                    minecraftClient.textRenderer.getWidth(defaultLine) > textWidth
                    ? minecraftClient.textRenderer.trimToWidth(defaultLine, textWidth) + "..."
                    : defaultLine
            ));

            textFieldWidget.setChangedListener(s -> {
                lineString = s;
                textFieldWidget.setPlaceholder(Text.literal(s));
            });

            isCentreWidget = CheckboxWidget.builder(Text.literal(isCentreText), minecraftClient.textRenderer)
                    .checked(defaultIsCentre)
                    .callback((checkbox, checked) -> isCentre = checked)
                    .build();


            isSmallWidget = CheckboxWidget.builder(Text.literal(isSmallText), minecraftClient.textRenderer)
                    .checked(defaultIsSmall)
                    .callback((checkbox, checked) -> isSmall = checked)
                    .build();

            addButton = ButtonWidget.builder(Text.literal("Add"),
                            (buttonWidget) -> callback.onAdd(this))
                    .size(BUTTON_SIZE, 20)
                    .tooltip(Tooltip.of(Text.literal("Add line")))
                    .build();

            deleteButton = ButtonWidget.builder(Text.literal("Del"),
                    (buttonWidget) -> callback.onDelete(this))
                    .size(BUTTON_SIZE, 20)
                    .tooltip(Tooltip.of(Text.literal("Delete line")))
                    .build();
        }

        public void setPosition(int x, int y, int fullWidth) {

            int textWidth = fullWidth - SPACING - BUTTON_SIZE * 2 - PADDING_QUART - SPACING - SMALL_TEXT_SPACING - CHECKBOX_SIZE - SPACING - CHECKBOX_SIZE - CENTRE_TEXT_SPACING - SPACING;

            textFieldWidget.setPosition(x, y);
            textFieldWidget.setWidth(textWidth);

            isCentreWidget.setPosition(
                    x + fullWidth - SPACING - BUTTON_SIZE * 2 - PADDING_QUART - SPACING - SMALL_TEXT_SPACING - CHECKBOX_SIZE - SPACING - CHECKBOX_SIZE - CENTRE_TEXT_SPACING,
                    y
            );

            isSmallWidget.setPosition(
                    x + fullWidth - SPACING - BUTTON_SIZE * 2 - PADDING_QUART - SPACING - SMALL_TEXT_SPACING - CHECKBOX_SIZE,
                    y
            );



            addButton.setPosition(
                    x + fullWidth - SPACING - BUTTON_SIZE * 2 - PADDING_QUART,
                    y
            );

            deleteButton.setPosition(
                    x + fullWidth - SPACING - BUTTON_SIZE,
                    y
            );
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            textFieldWidget.render(context, mouseX, mouseY, delta);
            isCentreWidget.render(context, mouseX, mouseY, delta);
            isSmallWidget.render(context, mouseX, mouseY, delta);
            addButton.render(context, mouseX, mouseY, delta);
            deleteButton.render(context, mouseX, mouseY, delta);

            this.renderTooltips(context, mouseX, mouseY, delta);
        }

        private void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
            if(textFieldWidget.isFocused()
                    && textFieldWidget.isMouseOver(mouseX, mouseY)) {
                context.drawTooltip(minecraftClient.textRenderer, Text.literal("You can also use placeholders. See wiki"), mouseX, mouseY);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (textFieldWidget.mouseClicked(mouseX, mouseY, button)) return true;
            if (isCentreWidget.mouseClicked(mouseX, mouseY, button)) return false;
            if (isSmallWidget.mouseClicked(mouseX, mouseY, button)) return false;
            if(addButton.mouseClicked(mouseX, mouseY, button)) return false;
            if(deleteButton.mouseClicked(mouseX, mouseY, button)) return false;
            return false;
        }

        public void setFocused(boolean focused) {
            textFieldWidget.setFocused(focused);
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return textFieldWidget.keyPressed(keyCode, scanCode, modifiers);
        }

        public boolean charTyped(char chr, int modifiers) {
            return textFieldWidget.charTyped(chr, modifiers);
        }


        public interface Callback {
            void onDelete(LineEntry lineEntry);
            void onAdd(LineEntry lineEntry);
        }
    }
}
