package dannypx.foe.screens.widget;

import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.store.CustomNotificationDataHandler;
import dannypx.foe.screens.interfaces.ScreenConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class EditCustomNotificationWidget extends ClickableWidget implements ScreenConstants {
    MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private final List<LineEntry> entries = new ArrayList<>();
    private LineEntry focusedEntry = null;

    private Text header;
    private final int headerHeight = 20;

    private final int textFieldHeight = 20;

    private final TextFieldWidget idTextField;
    public String newName;

    private final TextFieldWidget iconTextField;
    public String icon;

    public boolean hasSelectedOption = false;
    public String currentSelectedNotification = null;

    private int scrollOffset = 0;
    private final int scrollbarWidth = 6;

    public EditCustomNotificationWidget(int x, int y, int width, int height, Text header) {
        super(x, y, width, height, Text.empty());
        this.header = header;
        idTextField = new TextFieldWidget(
                minecraftClient.textRenderer,
                getX() + PADDING,
                getY() + headerHeight + PADDING,
                width / 2 - PADDING - PADDING_HALF,
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

        iconTextField = new TextFieldWidget(
                minecraftClient.textRenderer,
                getX() + width / 2 + minecraftClient.textRenderer.getWidth("Icon") + PADDING_HALF,
                getY() + headerHeight + PADDING,
                width / 2 - minecraftClient.textRenderer.getWidth("Icon") - PADDING - PADDING_HALF,
                textFieldHeight,
                Text.empty()
        );
        iconTextField.setMaxLength(Integer.MAX_VALUE);

        icon = "";

        iconTextField.setChangedListener(s -> {
            if (hasSelectedOption) {
                icon = s;
            }
            iconTextField.setPlaceholder(Text.literal(s));
        });

        iconTextField.setText("");
    }

    public List<LineEntry> getEntries() {
        return entries;
    }

    public void selectNotification(String id, CustomNotificationDataHandler.CustomNotification customNotification) {
        removeAllEntries();
        hasSelectedOption = true;
        newName = id;
        icon = customNotification.icon;
        currentSelectedNotification = id;
        header = Text.literal(id);
        idTextField.setText(id);
        idTextField.setPlaceholder(Text.literal(id));
        iconTextField.setText(customNotification.icon);
        iconTextField.setPlaceholder(Text.literal(customNotification.icon));

        customNotification.textLines.forEach(line -> this.addEntry(new LineEntry(
                line,
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
        return new EditCustomNotificationWidget.LineEntry(
                "Example text",
                width,
                getDefaultCallback()
        );
    }

    private LineEntry.Callback getDefaultCallback() {
        return new LineEntry.Callback() {
            @Override
            public void onDelete(LineEntry lineEntry) {
                CodeExecuterHandler.runLater(1, "EditCustomNotificationWidget > getDefaultCallback > onDelete", () -> removeEntry(lineEntry));
            }

            @Override
            public void onAdd(LineEntry lineEntry) {
                CodeExecuterHandler.runLater(1, "EditCustomNotificationWidget > getDefaultCallback > onAdd", () -> addNewEntry(entries.indexOf(lineEntry)));
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
        icon = "";
        currentSelectedNotification = null;
        iconTextField.setText("");
        iconTextField.setPlaceholder(Text.literal(""));
        idTextField.setText("");
        idTextField.setPlaceholder(Text.literal(""));
        header = Text.literal("No Notification Selected");

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
                "Icon",
                getX() + width / 2,
                getY() + PADDING + headerHeight + headerHeight / 2 - minecraftClient.textRenderer.fontHeight / 2,
                0xFFFFFF,
                true
        );

        idTextField.render(context, mouseX, mouseY, delta);
        iconTextField.render(context, mouseX, mouseY, delta);

        if(iconTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(minecraftClient.textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.empty(),
                    Text.literal("Must be an item").formatted(Formatting.GRAY),
                    Text.literal("using one of the following formats: ").formatted(Formatting.GRAY),
                    Text.literal("\"minecraft:<id>\"").formatted(Formatting.GOLD),
                    Text.literal("\"minecraft:<id>[<componentData>]\"").formatted(Formatting.GOLD)
            ), mouseX, mouseY);
        }

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
            idTextField.setFocused(true);
            iconTextField.setFocused(false);
            return true;
        }

        if (iconTextField.mouseClicked(mouseX, mouseY, button)) {
            if (focusedEntry != null) {
                focusedEntry.setFocused(false);
                focusedEntry = null;
            }
            iconTextField.setFocused(true);
            idTextField.setFocused(false);
            return true;
        }

        for (LineEntry entry : entries) {
            if (entry.mouseClicked(mouseX, mouseY, button)) {
                if (focusedEntry != null && focusedEntry != entry) focusedEntry.setFocused(false);
                focusedEntry = entry;
                entry.setFocused(true);
                idTextField.setFocused(false);
                iconTextField.setFocused(false);
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (idTextField.isFocused()) return idTextField.keyPressed(keyCode, scanCode, modifiers);
        if (iconTextField.isFocused()) return iconTextField.keyPressed(keyCode, scanCode, modifiers);
        if (focusedEntry != null) return focusedEntry.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (idTextField.isFocused()) return idTextField.charTyped(chr, modifiers);
        if (iconTextField.isFocused()) return iconTextField.charTyped(chr, modifiers);
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
        private final ButtonWidget addButton;
        private final ButtonWidget deleteButton;

        public String lineString;
        public int width;

        public static final int HEIGHT = 24;
        private static final int SPACING = 6;
        private static final int BUTTON_SIZE = 25;

        public LineEntry(String defaultLine, int width, Callback callback) {
            lineString = defaultLine;
            this.width = width;

            textFieldWidget = new TextFieldWidget(
                    minecraftClient.textRenderer,
                    0, 0,
                    0, 20,
                    Text.empty()
            );
            textFieldWidget.setMaxLength(Integer.MAX_VALUE);

            textFieldWidget.setText(defaultLine);
            int textWidth = width - BUTTON_SIZE * 2 - PADDING_QUART * 2 - SPACING - 20;
            textFieldWidget.setPlaceholder(Text.literal(
                    minecraftClient.textRenderer.getWidth(defaultLine) > textWidth
                    ? minecraftClient.textRenderer.trimToWidth(defaultLine, textWidth) + "..."
                    : defaultLine
            ));
            textFieldWidget.setChangedListener(s -> {
                lineString = s;
                textFieldWidget.setPlaceholder(Text.literal(s));
            });

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
            int textWidth = fullWidth - BUTTON_SIZE * 2 - PADDING_QUART * 2 - SPACING;

            textFieldWidget.setPosition(x, y);
            textFieldWidget.setWidth(textWidth);

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
