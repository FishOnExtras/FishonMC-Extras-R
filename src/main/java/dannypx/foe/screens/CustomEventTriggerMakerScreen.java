package dannypx.foe.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.store.CustomEventTriggerDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.screens.interfaces.ScreenConstants;
import dannypx.foe.screens.widget.ButtonListWidget;
import dannypx.foe.type.event.EventTrigger;
import dannypx.foe.type.tuple.Triplet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class CustomEventTriggerMakerScreen extends Screen implements ScreenConstants {
    //region Fields
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private final Screen parentScreen;

    private ButtonListWidget buttonList;
    private Map<String, ButtonListWidget.ButtonEntry> buttonEntryMap = new HashMap<>();
    private String selectedEventTriggerId;
    private CustomEventTriggerDataHandler.CustomEventTrigger selectedEventTrigger;


    private Text header;
    private final int widgetHeight = 20;

    private TextFieldWidget nameTextField;
    private CheckboxWidget useEventTriggerCheckBox;

    private final int sideWidth = 100;
    private TextFieldWidget eventTextField;
    private TextFieldWidget notificationToTriggerTextField;
    private TextFieldWidget chatNotificationToTriggerTextField;
    //endregion

    //region Methods
    public CustomEventTriggerMakerScreen(Screen parent) {
        super(Text.literal("Custom Chat Trigger Maker Screen"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.renderWidgets();
        this.resetFields();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBox(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        this.renderText(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY, delta);
        this.buttonList.render(context, mouseX, mouseY, delta);
    }

    private void renderTooltip(DrawContext context, int mouseX, int mouseY, float delta) {
        if(eventTextField.isMouseOver(mouseX, mouseY)) {
            List<Text> suggestions = new ArrayList<>(List.of(
                    Text.literal("Event Types").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("Supported Types").formatted(Formatting.GRAY)
            ));

            for (EventTrigger value : EventTrigger.values()) {
                suggestions.add(Text.literal("- " + value.name()).formatted(Formatting.YELLOW));
            }

            context.drawTooltip(textRenderer, suggestions, mouseX, mouseY);
        }

        if(notificationToTriggerTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.empty(),
                    Text.literal("- Notification Name").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }



        if(chatNotificationToTriggerTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.empty(),
                    Text.literal("- Chat Notification Name").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }
    }

    private void renderText(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(textRenderer,
                this.header,
                (BUTTON_WIDTH + PADDING * 2) + (minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2)) / 2,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2,
                0xFFFFFF
        );

        context.drawText(textRenderer,
                Text.literal("Name"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING),
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Event Type"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 2,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Trigger Notif."),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 3,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Trigger Chat Notif."),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 4,
                0xFFFFFF,
                true
        );
    }

    private void renderBox(DrawContext context, int mouseX, int mouseY, float delta)
    {
        context.fill(
                (BUTTON_WIDTH + PADDING * 2), 0,
                minecraftClient.getWindow().getScaledWidth(),
                minecraftClient.getWindow().getScaledHeight() - (BUTTON_HEIGHT + PADDING_HALF) - 3,
                0x99000000);
        context.drawHorizontalLine((BUTTON_WIDTH + PADDING * 2), minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight() - (BUTTON_HEIGHT + PADDING_HALF) - 3, 0xFF747474);
        context.drawVerticalLine((BUTTON_WIDTH + PADDING * 2), 0, minecraftClient.getWindow().getScaledHeight() - (BUTTON_HEIGHT + PADDING_HALF) - 3, 0xFF747474);
    }

    private void renderWidgets() {
        List<ClickableWidget> widgets = new ArrayList<>();

        widgets.add(this.saveBackButton());
        widgets.add(this.backButton());

        widgets.add(getButtonList());

        widgets.add(getNewButtonElementButton());
        widgets.add(getDeleteButtonElementButton());
        widgets.add(getImportButton());
        widgets.add(getExportButton());

        widgets.add(getNameTextField());
        widgets.add(getUseEventTriggerCheckBox());
        widgets.add(getEventTextField());
        widgets.add(getNotificationToTriggerTextField());
        widgets.add(getChatNotificationToTriggerTextField());

        widgets.forEach(this::addDrawableChild);
    }

    private ClickableWidget getNameTextField() {
        nameTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + widgetHeight + PADDING,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - (sideWidth + PADDING) - sideWidth,
                widgetHeight,
                Text.empty()
        );
        nameTextField.setMaxLength(Integer.MAX_VALUE);

        nameTextField.setChangedListener(s -> {
            if(selectedEventTriggerId != null) {
                nameTextField.setPlaceholder(Text.literal(s));
            }
        });

        return nameTextField;
    }

    private ClickableWidget getUseEventTriggerCheckBox() {
        useEventTriggerCheckBox = CheckboxWidget.builder(
                        Text.literal("Use Trigger"),
                        textRenderer
                )
                .pos(minecraftClient.getWindow().getScaledWidth() - PADDING - sideWidth
                        , PADDING + widgetHeight + PADDING)
                .checked(true)
                .callback((checkbox, checked) -> {})
                .build();
        return useEventTriggerCheckBox;
    }

    private ClickableWidget getEventTextField() {
        eventTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 2,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        eventTextField.setMaxLength(Integer.MAX_VALUE);

        eventTextField.setChangedListener(s -> {
            if(selectedEventTriggerId != null) {
                eventTextField.setPlaceholder(Text.literal(s));

                if (s.isEmpty()) {
                    eventTextField.setSuggestion(null);
                    return;
                }

                for (String event : Arrays.stream(EventTrigger.values()).map(Enum::name).toList()) {
                    if (event.toLowerCase().startsWith(s.toLowerCase()) &&
                            !event.equalsIgnoreCase(s)) {

                        eventTextField.setSuggestion(
                                event.substring(s.length())
                        );
                        return;
                    }
                }

                eventTextField.setSuggestion(null);
            }
        });

        return eventTextField;
    }

    private ClickableWidget getNotificationToTriggerTextField() {
        notificationToTriggerTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 3,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        notificationToTriggerTextField.setMaxLength(Integer.MAX_VALUE);

        notificationToTriggerTextField.setChangedListener(s -> {
            if(selectedEventTriggerId != null) {
                notificationToTriggerTextField.setPlaceholder(Text.literal(s));
            }
        });

        return notificationToTriggerTextField;
    }

    private ClickableWidget getChatNotificationToTriggerTextField() {
        chatNotificationToTriggerTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 4,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        chatNotificationToTriggerTextField.setMaxLength(Integer.MAX_VALUE);

        chatNotificationToTriggerTextField.setChangedListener(s -> {
            if(selectedEventTriggerId != null) {
                chatNotificationToTriggerTextField.setPlaceholder(Text.literal(s));
            }
        });

        return chatNotificationToTriggerTextField;
    }

    private ClickableWidget getNewButtonElementButton() {
        return ButtonWidget.builder(
                        Text.literal("Create Event Trigger"),
                        (button) -> {
                            String id = "Custom Event Trigger #" + UUID.randomUUID();

                            CustomEventTriggerDataHandler.instance().createNewCustomEventTrigger(id);

                            ButtonListWidget.ButtonEntry buttonEntry = createEventTriggerEntry(id);

                            buttonList.addEntry(buttonEntry);
                            buttonEntryMap.put(id, buttonEntry);
                        })
                .size(BUTTON_WIDTH / 2 - PADDING, BUTTON_HEIGHT)
                .position(PADDING_HALF, minecraftClient.getWindow().getScaledHeight() - PADDING_HALF - BUTTON_HEIGHT)
                .build();
    }

    private ClickableWidget getDeleteButtonElementButton() {
        return ButtonWidget.builder(
                        Text.literal("Delete Selected"),
                        (button) -> {
                            if(selectedEventTriggerId != null) {
                                CustomEventTriggerDataHandler.instance().deleteCustomEventTrigger(selectedEventTriggerId);

                                ButtonListWidget.ButtonEntry entry = buttonEntryMap.get(selectedEventTriggerId);

                                buttonList.removeEntry(entry);
                                buttonEntryMap.remove(selectedEventTriggerId);

                                selectedEventTriggerId = null;
                                resetFields();
                            }
                        })
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .position(PADDING + (BUTTON_WIDTH / 2 - PADDING_HALF), minecraftClient.getWindow().getScaledHeight() - PADDING_HALF - BUTTON_HEIGHT)
                .build();
    }

    private ClickableWidget getImportButton() {
        return ButtonWidget.builder(
                        Text.literal("Import"),
                        (button) -> {
                            String rawData = minecraftClient.keyboard.getClipboard();
                            try {
                                String json = TextHelper.decompress(Base64.getDecoder().decode(rawData));

                                Gson gson = new GsonBuilder().create();
                                Triplet<String, CustomEventTriggerDataHandler.CustomEventTrigger, Integer> data = gson.fromJson(json, TypeToken.getParameterized(Triplet.class, String.class, CustomEventTriggerDataHandler.CustomEventTrigger.class, Integer.class).getType());

                                if(data.value3() > FishOnMCExtras.EVENT_TRIGGER_VERSION) {
                                    SystemToast.add(minecraftClient.getToastManager(),
                                            SystemToast.Type.PERIODIC_NOTIFICATION,
                                            Text.literal("Fish On Extras Rebirth"),
                                            Text.literal("Could not Import. Imported Event Trigger is made on a newer version"));
                                    return;
                                }

                                if(CustomEventTriggerDataHandler.instance().getCustomEventTriggerData().eventTriggerList.containsKey(data.value1())) {
                                    CustomEventTriggerDataHandler.CustomEventTrigger trigger = data.value2();
                                    trigger.name = data.value1() + " (Duplicate)";

                                    data = Triplet.of(data.value1() + " (Duplicate)", trigger, data.value3());
                                }

                                String id = data.value1();

                                CustomEventTriggerDataHandler.instance().createNewCustomEventTrigger(data.value1(), data.value2());

                                ButtonListWidget.ButtonEntry buttonEntry = createEventTriggerEntry(id);

                                buttonList.addEntry(buttonEntry);
                                buttonEntryMap.put(id, buttonEntry);

                                SystemToast.add(minecraftClient.getToastManager(),
                                        SystemToast.Type.PERIODIC_NOTIFICATION,
                                        Text.literal("Fish On Extras Rebirth"),
                                        Text.literal("Imported Event Trigger"));
                            } catch (Exception e) {
                                LoggerHandler.error(e);

                                SystemToast.add(minecraftClient.getToastManager(),
                                        SystemToast.Type.PERIODIC_NOTIFICATION,
                                        Text.literal("Fish On Extras Rebirth"),
                                        Text.literal("Could not Import. Data invalid"));
                            }
                        })
                .size(BUTTON_WIDTH / 2 - PADDING, BUTTON_HEIGHT)
                .position(PADDING_HALF, minecraftClient.getWindow().getScaledHeight() - PADDING_HALF - BUTTON_HEIGHT * 2 - PADDING_HALF)
                .tooltip(Tooltip.of(Text.literal("Imports from the code on your clipboard")))
                .build();
    }

    private ClickableWidget getExportButton() {
        return ButtonWidget.builder(
                        Text.literal("Export Selected"),
                        (button) -> {
                            if(selectedEventTriggerId != null) {
                                try {
                                    Triplet<String, CustomEventTriggerDataHandler.CustomEventTrigger, Integer> dataButton = Triplet.of(
                                            selectedEventTriggerId,
                                            selectedEventTrigger,
                                            FishOnMCExtras.EVENT_TRIGGER_VERSION
                                    );

                                    String rawData = Base64.getEncoder().encodeToString(
                                            TextHelper.compress(new GsonBuilder().create().toJson(dataButton))
                                    );

                                    String dataToCopy = "**Custom Event Trigger: **" + selectedEventTriggerId + "\n" +
                                            "```\n" +
                                            rawData + "\n" +
                                            "```\n" +
                                            "-# Using Event Trigger version: " + "`v" + FishOnMCExtras.EVENT_TRIGGER_VERSION + "`";

                                    minecraftClient.keyboard.setClipboard(dataToCopy);

                                    SystemToast.add(minecraftClient.getToastManager(),
                                            SystemToast.Type.PERIODIC_NOTIFICATION,
                                            Text.literal("Fish On Extras Rebirth"),
                                            Text.literal("Exported Button on your clipboard"));
                                } catch (Exception e) {
                                    LoggerHandler.error(e);

                                    SystemToast.add(minecraftClient.getToastManager(),
                                            SystemToast.Type.PERIODIC_NOTIFICATION,
                                            Text.literal("Fish On Extras Rebirth"),
                                            Text.literal("An error has occurred"));
                                }
                            }
                        })
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .position(PADDING + (BUTTON_WIDTH / 2 - PADDING_HALF), minecraftClient.getWindow().getScaledHeight() - PADDING_HALF - BUTTON_HEIGHT * 2 - PADDING_HALF)
                .tooltip(Tooltip.of(Text.literal("Save first before exporting")))
                .build();
    }

    private ClickableWidget getButtonList() {
        buttonList = new ButtonListWidget(
                client,
                (BUTTON_WIDTH + PADDING * 2),
                height - ScreenConstants.BUTTON_HEIGHT * 2 - PADDING - PADDING_HALF,
                0,
                BUTTON_HEIGHT + PADDING_HALF,
                BUTTON_HEIGHT,
                "Custom Event Triggers"
        );

        CustomEventTriggerDataHandler.instance().getCustomEventTriggerData().eventTriggerList.forEach((name, eventTrigger) -> {
            ButtonListWidget.ButtonEntry buttonEntry = createEventTriggerEntry(eventTrigger.name);

            buttonList.addEntry(buttonEntry);
            buttonEntryMap.put(eventTrigger.name, buttonEntry);
        });

        return buttonList;
    }

    private ButtonWidget saveBackButton() {
        return ButtonWidget.builder(Text.literal("Save and Return"), button -> {
            if(selectedEventTriggerId != null) {
                if(nameTextField.getText().isBlank()) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Event Trigger name is empty"));

                    return;
                }

                if(!Objects.equals(selectedEventTriggerId, nameTextField.getText())
                        && CustomEventTriggerDataHandler.instance().getCustomEventTriggerData().eventTriggerList.containsKey(nameTextField.getText())
                ) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Event Trigger name already exist"));

                    return;
                }

                if(Arrays.stream(EventTrigger.values()).noneMatch(eventTrigger ->
                    eventTrigger.name().equals(eventTextField.getText())
                )) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Event does not exist"));

                    return;
                }

                CustomEventTriggerDataHandler.instance().updateEventTrigger(selectedEventTriggerId,
                        nameTextField.getText(),
                        EventTrigger.valueOf(eventTextField.getText()),
                        notificationToTriggerTextField.getText(),
                        chatNotificationToTriggerTextField.getText(),
                        useEventTriggerCheckBox.isChecked());
            }
                    this.close();
                })
                .position(width - PADDING_HALF - BUTTON_WIDTH / 2, height - PADDING_HALF - BUTTON_HEIGHT)
                .size(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
                .build();
    }

    private ButtonWidget backButton() {
        return ButtonWidget.builder(Text.literal("Return"), button ->
                    this.close())
                .position(width - (PADDING_HALF + BUTTON_WIDTH / 2) * 2, height - PADDING_HALF - BUTTON_HEIGHT)
                .size(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
                .build();
    }

    private ButtonListWidget.ButtonEntry createEventTriggerEntry(String id) {
        return new ButtonListWidget.ButtonEntry(
                ButtonWidget.builder(
                        Text.literal(id),
                        button -> {
                            selectedEventTrigger = CustomEventTriggerDataHandler.instance().getCustomEventTriggerData().eventTriggerList.get(id);

                            if(selectedEventTrigger != null) {
                                selectedEventTriggerId = id;
                                this.setFields();
                            }
                        }
                ).width(BUTTON_WIDTH).build()
        );
    }

    private void setFields() {
        this.header = Text.literal(selectedEventTriggerId);
        nameTextField.setText(selectedEventTriggerId);
        nameTextField.setPlaceholder(Text.literal(selectedEventTriggerId));

        if(selectedEventTrigger != null) {
            if(selectedEventTrigger.useEventTrigger != useEventTriggerCheckBox.isChecked()) {
                useEventTriggerCheckBox.onPress();
            }

            eventTextField.setText(selectedEventTrigger.event.name());
            eventTextField.setPlaceholder(Text.literal(selectedEventTrigger.event.name()));

            notificationToTriggerTextField.setText(selectedEventTrigger.notificationToTrigger);
            notificationToTriggerTextField.setPlaceholder(Text.literal(selectedEventTrigger.notificationToTrigger));

            chatNotificationToTriggerTextField.setText(selectedEventTrigger.chatNotificationToTrigger);
            chatNotificationToTriggerTextField.setPlaceholder(Text.literal(selectedEventTrigger.chatNotificationToTrigger != null ? selectedEventTrigger.chatNotificationToTrigger : ""));

        }
    }

    private void resetFields() {
        this.header = Text.literal("No Event Trigger Selected");

        nameTextField.setText("");
        nameTextField.setPlaceholder(Text.literal(""));

        if(useEventTriggerCheckBox.isChecked()) {
            useEventTriggerCheckBox.onPress();
        }

        eventTextField.setText("");
        eventTextField.setPlaceholder(Text.literal(""));

        notificationToTriggerTextField.setText("");
        notificationToTriggerTextField.setPlaceholder(Text.literal(""));

        chatNotificationToTriggerTextField.setText("");
        chatNotificationToTriggerTextField.setPlaceholder(Text.literal(""));

        selectedEventTrigger = null;
        selectedEventTriggerId = null;
    }

    @Override
    public void close() {
        this.minecraftClient.setScreen(this.parentScreen);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            String current = eventTextField.getText();

            for (String event : Arrays.stream(EventTrigger.values()).map(Enum::name).toList()) {
                if (event.toLowerCase().startsWith(current.toLowerCase())) {
                    eventTextField.setText(event);
                    eventTextField.setSuggestion(null);
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    //endregion
}
