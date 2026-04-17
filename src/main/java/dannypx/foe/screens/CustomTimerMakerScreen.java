package dannypx.foe.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.logic.TimerHandler;
import dannypx.foe.handler.store.CustomTimerDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.screens.interfaces.ScreenConstants;
import dannypx.foe.screens.widget.ButtonListWidget;
import dannypx.foe.type.tuple.Quartet;
import dannypx.foe.type.tuple.Triplet;
import dannypx.foe.type.type_adapter.PatternAdapter;
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

import java.util.*;
import java.util.regex.Pattern;

public class CustomTimerMakerScreen extends Screen implements ScreenConstants {
    //region Fields
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private final Screen parentScreen;

    private ButtonListWidget buttonList;
    private Map<String, ButtonListWidget.ButtonEntry> buttonEntryMap = new HashMap<>();
    private String selectedTimerId;
    private CustomTimerDataHandler.CustomTimer selectedTimer;


    private Text header;
    private final int widgetHeight = 20;

    private TextFieldWidget nameTextField;
    private CheckboxWidget useTimerCheckBox;
    private CheckboxWidget isPeriodCheckBox;

    private final int sideWidth = 100;
    private TextFieldWidget timerTextField;
    private TextFieldWidget offTimerTextField;
    private TextFieldWidget offsetTextField;
    private TextFieldWidget notificationToTriggerTextField;
    private TextFieldWidget notificationToTriggerEndTextField;
    private TextFieldWidget chatNotificationToTriggerTextField;
    private TextFieldWidget chatNotificationToTriggerEndTextField;
    private TextFieldWidget cleanUpChatTriggersTextField;
    //endregion

    //region Methods
    public CustomTimerMakerScreen(Screen parent) {
        super(Text.literal("Custom Timer Maker Screen"));
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
        if(isPeriodCheckBox.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("'period' mode is for timers that require a period of ON and OFF time").formatted(Formatting.GRAY),
                    Text.literal("After x seconds of ON time, the timer will be on OFF mode for x seconds, and then back to ON again").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(timerTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Time in seconds").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("When not in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Timer for every x seconds").formatted(Formatting.DARK_GRAY),
                    Text.empty(),
                    Text.literal("When in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Time period in seconds whenever it is 'ON'").formatted(Formatting.DARK_GRAY)
            ), mouseX, mouseY);
        }

        if(offTimerTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Time in seconds").formatted(Formatting.GRAY),
                    Text.literal("Only for 'period' mode").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("When in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Time period in seconds whenever it is 'OFF'").formatted(Formatting.DARK_GRAY)
            ), mouseX, mouseY);
        }

        if(offsetTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Time in seconds").formatted(Formatting.GRAY),
                    Text.literal("Offset of the timer for alignment").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(notificationToTriggerTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.empty(),
                    Text.literal("When not in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Triggers when timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("When in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Trigger when OFF timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("- Notification Name").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(notificationToTriggerEndTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.literal("Only for 'period' mode").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("When in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Trigger when ON timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("- Notification Name").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(chatNotificationToTriggerTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.empty(),
                    Text.literal("When not in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Triggers when timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("When in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Trigger when OFF timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("- Chat Notification Name").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(chatNotificationToTriggerEndTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.literal("Only for 'period' mode").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("When in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Trigger when ON timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("- Chat Notification Name").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(cleanUpChatTriggersTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Optional").formatted(Formatting.DARK_GRAY, Formatting.ITALIC),
                    Text.empty(),
                    Text.literal("When not in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Clean chat triggers when timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("When in 'period' mode").formatted(Formatting.GRAY),
                    Text.literal("Clean chat triggers when ON timer hits 0").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("Split multiple chat triggers with a comma").formatted(Formatting.GRAY)
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
                Text.literal("Timer"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 2,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Off Timer"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 3,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Offset"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 4,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Trigger Notif."),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 5,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Trigger Notif. End"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 6,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Trigger C.Notif."),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 7,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Trigger C.Notif. End"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 8,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Clear Triggers"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 9,
                0xFFFFFF,
                true
        );
        try {
            if(selectedTimerId != null) {
                long timeSeconds = System.currentTimeMillis() / 1000;
                long adjustedWithOffset = timeSeconds + Integer.parseInt(offsetTextField.getText());

                if(isPeriodCheckBox.isChecked()) {
                    long timer = Integer.parseInt(timerTextField.getText());
                    long offTimer = Integer.parseInt(offTimerTextField.getText());
                    long cycle = timer + offTimer;
                    long pos = (adjustedWithOffset - offTimer) % cycle;
                    long remainingOn = cycle - pos;
                    long midPos = adjustedWithOffset % cycle;
                    long remainingOff = cycle - midPos;

                    Triplet<Long, Long, Long> remainingTime = getTime(remainingOn);
                    Triplet<Long, Long, Long> remainingTimeMid = getTime(remainingOff);

                    boolean isOnTimer = remainingOn < timer;

                    Text onTimerText = TextHelper.concat(
                            Text.literal("Timer till ").formatted(Formatting.GRAY),
                            Text.literal("ON").formatted(Formatting.GREEN),
                            Text.literal(" period ends: ").formatted(Formatting.GRAY),
                            Text.literal(String.valueOf(remainingTime.value3())).formatted(Formatting.YELLOW),
                            Text.literal(":").formatted(Formatting.YELLOW),
                            Text.literal(String.format("%02d", remainingTime.value2())).formatted(Formatting.YELLOW),
                            Text.literal(":").formatted(Formatting.YELLOW),
                            Text.literal(String.format("%02d", remainingTime.value1())).formatted(Formatting.YELLOW)
                    );

                    Text offTimerText = TextHelper.concat(
                            Text.literal("Timer till ").formatted(Formatting.GRAY),
                            Text.literal("OFF").formatted(Formatting.RED),
                            Text.literal(" period ends: ").formatted(Formatting.GRAY),
                            Text.literal(String.valueOf(remainingTimeMid.value3())).formatted(Formatting.YELLOW),
                            Text.literal(":").formatted(Formatting.YELLOW),
                            Text.literal(String.format("%02d", remainingTimeMid.value2())).formatted(Formatting.YELLOW),
                            Text.literal(":").formatted(Formatting.YELLOW),
                            Text.literal(String.format("%02d", remainingTimeMid.value1())).formatted(Formatting.YELLOW)
                    );

                    context.drawText(textRenderer,
                            onTimerText,
                            (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                            PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 10,
                            0xFFFFFF,
                            true
                    );

                    context.drawText(textRenderer,
                            offTimerText,
                            (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                            PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 10 + (textRenderer.fontHeight + PADDING_QUART) * 1,
                            0xFFFFFF,
                            true
                    );

                    Text isOnTimerText = TextHelper.concat(
                            Text.literal("Currently in ").formatted(Formatting.GRAY),
                            isOnTimer ? Text.literal("ON").formatted(Formatting.GREEN) : Text.literal("OFF").formatted(Formatting.RED),
                            Text.literal(" period").formatted(Formatting.GRAY)
                    );

                    context.drawText(textRenderer,
                            isOnTimerText,
                            (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                            PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 10 + (textRenderer.fontHeight + PADDING_QUART) * 2,
                            0xFFFFFF,
                            true
                    );
                } else {
                    long interval = Integer.parseInt(timerTextField.getText());
                    long pos = adjustedWithOffset % interval;
                    long remainingOn = interval - pos;

                    Triplet<Long, Long, Long> remainingTime = getTime(remainingOn);

                    Text onTimerText = TextHelper.concat(
                            Text.literal("Timer: ").formatted(Formatting.GRAY),
                            Text.literal(String.valueOf(remainingTime.value3())).formatted(Formatting.YELLOW),
                            Text.literal(":").formatted(Formatting.YELLOW),
                            Text.literal(String.format("%02d", remainingTime.value2())).formatted(Formatting.YELLOW),
                            Text.literal(":").formatted(Formatting.YELLOW),
                            Text.literal(String.format("%02d", remainingTime.value1())).formatted(Formatting.YELLOW)
                    );

                    context.drawText(textRenderer,
                            onTimerText,
                            (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                            PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 10,
                            0xFFFFFF,
                            true
                    );

                }
            }
        } catch (Exception ignored) {

        }
    }

    private Triplet<Long, Long, Long> getTime(long seconds) {
        long hour = seconds / 3600;
        long minute = (seconds % 3600) / 60;
        long second = seconds % 60;

        return Triplet.of(second, minute, hour);
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
        widgets.add(getUseTimerCheckBox());
        widgets.add(getIsPeriodCheckBox());
        widgets.add(getTimerTextField());
        widgets.add(getOffTimerTextField());
        widgets.add(getOffsetTextField());
        widgets.add(getNotificationToTriggerTextField());
        widgets.add(getNotificationToTriggerEndTextField());
        widgets.add(getChatNotificationToTriggerTextField());
        widgets.add(getChatNotificationToTriggerEndTextField());
        widgets.add(getCleanUpChatTriggersTextField());

        widgets.forEach(this::addDrawableChild);
    }

    private ClickableWidget getNameTextField() {
        nameTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + widgetHeight + PADDING,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - (sideWidth + PADDING) * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        nameTextField.setMaxLength(Integer.MAX_VALUE);

        nameTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                nameTextField.setPlaceholder(Text.literal(s));
            }
        });

        return nameTextField;
    }

    private ClickableWidget getUseTimerCheckBox() {
        useTimerCheckBox = CheckboxWidget.builder(
                        Text.literal("Use Timer"),
                        textRenderer
                )
                .pos(minecraftClient.getWindow().getScaledWidth() - (PADDING + sideWidth) * 2
                        , PADDING + widgetHeight + PADDING)
                .checked(true)
                .callback((checkbox, checked) -> {})
                .build();
        return useTimerCheckBox;
    }

    private ClickableWidget getIsPeriodCheckBox() {
        isPeriodCheckBox = CheckboxWidget.builder(
                        Text.literal("Is Period"),
                        textRenderer
                )
                .pos(minecraftClient.getWindow().getScaledWidth() - (PADDING + sideWidth)
                        , PADDING + widgetHeight + PADDING)
                .checked(false)
                .callback((checkbox, checked) -> {
                    if(checkbox.isChecked()) {
                        offTimerTextField.setMaxLength(Integer.MAX_VALUE);
                        offTimerTextField.setText(String.valueOf(60));
                        offTimerTextField.setPlaceholder(Text.literal(String.valueOf(60)));
                        notificationToTriggerEndTextField.setMaxLength(Integer.MAX_VALUE);
                    } else {
                        offTimerTextField.setMaxLength(0);
                        offTimerTextField.setText("");
                        offTimerTextField.setPlaceholder(Text.literal(""));
                        notificationToTriggerEndTextField.setMaxLength(0);
                        notificationToTriggerEndTextField.setText("");
                        notificationToTriggerEndTextField.setPlaceholder(Text.literal(""));
                    }
                })
                .build();
        return isPeriodCheckBox;
    }

    private ClickableWidget getTimerTextField() {
        timerTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 2,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        timerTextField.setMaxLength(Integer.MAX_VALUE);

        timerTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                timerTextField.setPlaceholder(Text.literal(s));
            }
        });

        return timerTextField;
    }

    private ClickableWidget getOffTimerTextField() {
        offTimerTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 3,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        offTimerTextField.setMaxLength(0);

        offTimerTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                offTimerTextField.setPlaceholder(Text.literal(s));
            }
        });

        return offTimerTextField;
    }

    private ClickableWidget getOffsetTextField() {
        offsetTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 4,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        offsetTextField.setMaxLength(Integer.MAX_VALUE);

        offsetTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                offsetTextField.setPlaceholder(Text.literal(s));
            }
        });

        return offsetTextField;
    }

    private ClickableWidget getNotificationToTriggerTextField() {
        notificationToTriggerTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 5,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        notificationToTriggerTextField.setMaxLength(Integer.MAX_VALUE);

        notificationToTriggerTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                notificationToTriggerTextField.setPlaceholder(Text.literal(s));
            }
        });

        return notificationToTriggerTextField;
    }

    private ClickableWidget getNotificationToTriggerEndTextField() {
        notificationToTriggerEndTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 6,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        notificationToTriggerEndTextField.setMaxLength(0);

        notificationToTriggerEndTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                notificationToTriggerEndTextField.setPlaceholder(Text.literal(s));
            }
        });

        return notificationToTriggerEndTextField;
    }

    private ClickableWidget getChatNotificationToTriggerTextField() {
        chatNotificationToTriggerTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 7,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        chatNotificationToTriggerTextField.setMaxLength(Integer.MAX_VALUE);

        chatNotificationToTriggerTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                chatNotificationToTriggerTextField.setPlaceholder(Text.literal(s));
            }
        });

        return chatNotificationToTriggerTextField;
    }

    private ClickableWidget getChatNotificationToTriggerEndTextField() {
        chatNotificationToTriggerEndTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 8,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        chatNotificationToTriggerEndTextField.setMaxLength(0);

        chatNotificationToTriggerEndTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                chatNotificationToTriggerEndTextField.setPlaceholder(Text.literal(s));
            }
        });

        return chatNotificationToTriggerEndTextField;
    }

    private ClickableWidget getCleanUpChatTriggersTextField() {
        cleanUpChatTriggersTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 9,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        cleanUpChatTriggersTextField.setMaxLength(Integer.MAX_VALUE);

        cleanUpChatTriggersTextField.setChangedListener(s -> {
            if(selectedTimerId != null) {
                cleanUpChatTriggersTextField.setPlaceholder(Text.literal(s));
            }
        });

        return cleanUpChatTriggersTextField;
    }

    private ClickableWidget getNewButtonElementButton() {
        return ButtonWidget.builder(
                        Text.literal("Create Timer"),
                        (button) -> {
                            String id = "Custom Timer " + UUID.randomUUID();

                            CustomTimerDataHandler.instance().createNewCustomTimer(id);

                            ButtonListWidget.ButtonEntry buttonEntry = createTimerEntry(id);

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
                            if(selectedTimerId != null) {
                                CustomTimerDataHandler.instance().deleteCustomTimer(selectedTimerId);
                                TimerHandler.instance().initTimers();

                                ButtonListWidget.ButtonEntry entry = buttonEntryMap.get(selectedTimerId);

                                buttonList.removeEntry(entry);
                                buttonEntryMap.remove(selectedTimerId);

                                selectedTimerId = null;
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

                                Gson gson = new GsonBuilder().registerTypeAdapter(Pattern.class, new PatternAdapter()).create();
                                Quartet<String, CustomTimerDataHandler.CustomTimer, Boolean, Integer> data = gson.fromJson(json, TypeToken.getParameterized(Triplet.class, String.class, CustomTimerDataHandler.CustomTimer.class, Integer.class).getType());

                                if(data.value3()) {
                                    data = gson.fromJson(json, TypeToken.getParameterized(Triplet.class, String.class, CustomTimerDataHandler.CustomTimerPeriod.class, Integer.class).getType());
                                }

                                if(data.value4() > FishOnMCExtras.TIMER_VERSION) {
                                    SystemToast.add(minecraftClient.getToastManager(),
                                            SystemToast.Type.PERIODIC_NOTIFICATION,
                                            Text.literal("Fish On Extras Rebirth"),
                                            Text.literal("Could not Import. Imported Timer is made on a newer version"));
                                    return;
                                }

                                if(CustomTimerDataHandler.instance().getCustomTimerData().timerList.containsKey(data.value1())) {
                                    CustomTimerDataHandler.CustomTimer trigger = data.value2();
                                    trigger.name = data.value1() + " (Duplicate)";

                                    data = Quartet.of(data.value1() + " (Duplicate)", trigger, data.value3(), data.value4());
                                }

                                String id = data.value1();

                                CustomTimerDataHandler.instance().createNewCustomTimer(data.value1(), data.value2());
                                TimerHandler.instance().initTimers();

                                ButtonListWidget.ButtonEntry buttonEntry = createTimerEntry(id);

                                buttonList.addEntry(buttonEntry);
                                buttonEntryMap.put(id, buttonEntry);

                                SystemToast.add(minecraftClient.getToastManager(),
                                        SystemToast.Type.PERIODIC_NOTIFICATION,
                                        Text.literal("Fish On Extras Rebirth"),
                                        Text.literal("Imported Timer"));
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
                            if(selectedTimerId != null) {
                                try {
                                    Quartet<String, CustomTimerDataHandler.CustomTimer, Boolean, Integer> dataButton = Quartet.of(
                                            selectedTimerId,
                                            selectedTimer,
                                            selectedTimer instanceof CustomTimerDataHandler.CustomTimerPeriod,
                                            FishOnMCExtras.TIMER_VERSION
                                    );

                                    String rawData = Base64.getEncoder().encodeToString(
                                            TextHelper.compress(new GsonBuilder().registerTypeAdapter(Pattern.class, new PatternAdapter()).create().toJson(dataButton))
                                    );

                                    String dataToCopy = "**Custom Timer: **" + selectedTimerId + "\n" +
                                            "```\n" +
                                            rawData + "\n" +
                                            "```\n" +
                                            "-# Using Timer version: " + "`v" + FishOnMCExtras.TIMER_VERSION + "`";

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
                "Custom Timers"
        );

        CustomTimerDataHandler.instance().getCustomTimerData().timerList.forEach((name, timer) -> {
            ButtonListWidget.ButtonEntry buttonEntry = createTimerEntry(timer.name);

            buttonList.addEntry(buttonEntry);
            buttonEntryMap.put(timer.name, buttonEntry);
        });

        return buttonList;
    }

    private ButtonWidget saveBackButton() {
        return ButtonWidget.builder(Text.literal("Save and Return"), button -> {
            if(selectedTimerId != null) {
                if(nameTextField.getText().isBlank()) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Timer name is empty"));

                    return;
                }

                if(!Objects.equals(selectedTimerId, nameTextField.getText())
                        && CustomTimerDataHandler.instance().getCustomTimerData().timerList.containsKey(nameTextField.getText())
                ) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Timer name already exist"));

                    return;
                }

                try {
                    Integer.parseInt(timerTextField.getText());
                } catch (NumberFormatException e) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Timer is not correct format"));

                    LoggerHandler.error(e);

                    return;
                }

                try {
                    if(isPeriodCheckBox.isChecked()) {
                        Integer.parseInt(offTimerTextField.getText());
                    }
                } catch (NumberFormatException e) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Off Timer is not correct format"));

                    LoggerHandler.error(e);

                    return;
                }

                try {
                    Integer.parseInt(offsetTextField.getText());
                } catch (NumberFormatException e) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Offset is not correct format"));

                    LoggerHandler.error(e);

                    return;
                }

                if(isPeriodCheckBox.isChecked()) {
                    CustomTimerDataHandler.instance().updateTimer(selectedTimerId,
                            nameTextField.getText(),
                            Integer.parseInt(timerTextField.getText()),
                            Integer.parseInt(offTimerTextField.getText()),
                            Integer.parseInt(offsetTextField.getText()),
                            notificationToTriggerTextField.getText(),
                            notificationToTriggerEndTextField.getText(),
                            chatNotificationToTriggerTextField.getText(),
                            chatNotificationToTriggerEndTextField.getText(),
                            cleanUpChatTriggersTextField.getText(),
                            useTimerCheckBox.isChecked(),
                            isPeriodCheckBox.isChecked());
                } else {
                    CustomTimerDataHandler.instance().updateTimer(selectedTimerId,
                            nameTextField.getText(),
                            Integer.parseInt(timerTextField.getText()),
                            Integer.parseInt(offsetTextField.getText()),
                            notificationToTriggerTextField.getText(),
                            chatNotificationToTriggerTextField.getText(),
                            cleanUpChatTriggersTextField.getText(),
                            useTimerCheckBox.isChecked(),
                            isPeriodCheckBox.isChecked());
                }

                TimerHandler.instance().initTimers();
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

    private ButtonListWidget.ButtonEntry createTimerEntry(String id) {
        return new ButtonListWidget.ButtonEntry(
                ButtonWidget.builder(
                        Text.literal(id),
                        button -> {
                            selectedTimer = CustomTimerDataHandler.instance().getCustomTimerData().timerList.get(id);

                            if(selectedTimer != null) {
                                selectedTimerId = id;
                                this.setFields();
                            }
                        }
                ).width(BUTTON_WIDTH).build()
        );
    }

    private void setFields() {
        this.header = Text.literal(selectedTimerId);
        nameTextField.setText(selectedTimerId);
        nameTextField.setPlaceholder(Text.literal(selectedTimerId));

        if(selectedTimer != null) {
            if(selectedTimer.useTimer != useTimerCheckBox.isChecked()) {
                useTimerCheckBox.onPress();
            }

            if(selectedTimer.isPeriod != isPeriodCheckBox.isChecked()) {
                isPeriodCheckBox.onPress();
            }

            timerTextField.setText(String.valueOf(selectedTimer.timer));
            timerTextField.setPlaceholder(Text.literal(String.valueOf(selectedTimer.timer)));

            offsetTextField.setText(String.valueOf(selectedTimer.offset));
            offsetTextField.setPlaceholder(Text.literal(String.valueOf(selectedTimer.offset)));

            notificationToTriggerTextField.setText(selectedTimer.notificationToTrigger);
            notificationToTriggerTextField.setPlaceholder(Text.literal(selectedTimer.notificationToTrigger));

            chatNotificationToTriggerTextField.setText(selectedTimer.chatNotificationToTrigger);
            chatNotificationToTriggerTextField.setPlaceholder(Text.literal(selectedTimer.chatNotificationToTrigger != null ? selectedTimer.chatNotificationToTrigger : ""));

            cleanUpChatTriggersTextField.setText(selectedTimer.cleanUpChatTrigger);
            cleanUpChatTriggersTextField.setPlaceholder(Text.literal(selectedTimer.cleanUpChatTrigger));

            CodeExecuterHandler.runLater(1, "CustomTimerMakerScreen > setFields", () -> {
                if(selectedTimer.isPeriod && selectedTimer instanceof CustomTimerDataHandler.CustomTimerPeriod selectedTimerPeriod) {
                    offTimerTextField.setMaxLength(Integer.MAX_VALUE);
                    offTimerTextField.setText(String.valueOf(selectedTimerPeriod.offTimer));
                    offTimerTextField.setPlaceholder(Text.literal(String.valueOf(selectedTimerPeriod.offTimer)));

                    notificationToTriggerEndTextField.setMaxLength(Integer.MAX_VALUE);
                    notificationToTriggerEndTextField.setText(selectedTimerPeriod.notificationToTriggerEnd);
                    notificationToTriggerEndTextField.setPlaceholder(Text.literal(selectedTimerPeriod.notificationToTriggerEnd));

                    chatNotificationToTriggerEndTextField.setMaxLength(Integer.MAX_VALUE);
                    chatNotificationToTriggerEndTextField.setText(selectedTimerPeriod.chatNotificationToTriggerEnd);
                    chatNotificationToTriggerEndTextField.setPlaceholder(Text.literal(selectedTimerPeriod.chatNotificationToTriggerEnd != null ? selectedTimerPeriod.chatNotificationToTriggerEnd : ""));
                } else {
                    offTimerTextField.setMaxLength(0);
                    offTimerTextField.setText("");
                    offTimerTextField.setPlaceholder(Text.literal(""));

                    notificationToTriggerEndTextField.setMaxLength(0);
                    notificationToTriggerEndTextField.setText("");
                    notificationToTriggerEndTextField.setPlaceholder(Text.literal(""));

                    chatNotificationToTriggerEndTextField.setMaxLength(0);
                    chatNotificationToTriggerEndTextField.setText("");
                    chatNotificationToTriggerEndTextField.setPlaceholder(Text.literal(""));
                }
            });
        }
    }

    private void resetFields() {
        this.header = Text.literal("No Timer Selected");

        nameTextField.setText("");
        nameTextField.setPlaceholder(Text.literal(""));

        if(useTimerCheckBox.isChecked()) {
            useTimerCheckBox.onPress();
        }

        if(isPeriodCheckBox.isChecked()) {
            isPeriodCheckBox.onPress();
        }

        timerTextField.setText("");
        timerTextField.setPlaceholder(Text.literal(""));

        offTimerTextField.setText("");
        offTimerTextField.setPlaceholder(Text.literal(""));

        offsetTextField.setText("");
        offsetTextField.setPlaceholder(Text.literal(""));

        notificationToTriggerTextField.setText("");
        notificationToTriggerTextField.setPlaceholder(Text.literal(""));

        notificationToTriggerEndTextField.setText("");
        notificationToTriggerEndTextField.setPlaceholder(Text.literal(""));

        chatNotificationToTriggerTextField.setText("");
        chatNotificationToTriggerTextField.setPlaceholder(Text.literal(""));

        chatNotificationToTriggerEndTextField.setText("");
        chatNotificationToTriggerEndTextField.setPlaceholder(Text.literal(""));

        cleanUpChatTriggersTextField.setText("");
        cleanUpChatTriggersTextField.setPlaceholder(Text.literal(""));

        selectedTimer = null;
        selectedTimerId = null;
    }

    @Override
    public void close() {
        this.minecraftClient.setScreen(this.parentScreen);
    }
    //endregion
}
