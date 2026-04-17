package dannypx.foe.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.fetch.ChatHandler;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.store.CustomChatNotificationDataHandler;
import dannypx.foe.handler.store.CustomChatTriggerDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.screens.interfaces.ScreenConstants;
import dannypx.foe.screens.widget.ButtonListWidget;
import dannypx.foe.type.tuple.Triplet;
import dannypx.foe.type.type_adapter.PatternAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomChatNotificationMakerScreen extends Screen implements ScreenConstants {
    //region Fields
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private final Screen parentScreen;

    private ButtonListWidget buttonList;
    private Map<String, ButtonListWidget.ButtonEntry> buttonEntryMap = new HashMap<>();
    private String selectedChatNotificationId;

    private Text header;
    private final int widgetHeight = 20;

    private TextFieldWidget nameTextField;

    private final int sideWidth = 100;
    private TextFieldWidget stringTextField;
    private String stringField;
    //endregion

    //region Methods
    public CustomChatNotificationMakerScreen(Screen parent) {
        super(Text.literal("Custom Chat Chat Notification Maker Screen"));
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
        if(stringTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("You can also use placeholders. See wiki")
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
                Text.literal("Notif. Text"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 2,
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
        widgets.add(getStringTextField());

        widgets.add(this.wikiButton());

        widgets.forEach(this::addDrawableChild);
    }

    private ClickableWidget getNameTextField() {
        nameTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + widgetHeight + PADDING,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2  - sideWidth,
                widgetHeight,
                Text.empty()
        );
        nameTextField.setMaxLength(Integer.MAX_VALUE);

        nameTextField.setChangedListener(s -> {
            if(selectedChatNotificationId != null) {
                nameTextField.setPlaceholder(Text.literal(s));
            }
        });

        return nameTextField;
    }

    private ClickableWidget getStringTextField() {
        stringTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 2,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        stringTextField.setMaxLength(Integer.MAX_VALUE);

        stringTextField.setChangedListener(s -> {
            if(selectedChatNotificationId != null) {
                stringField = s;
                stringTextField.setPlaceholder(Text.literal(s));
            }
        });

        return stringTextField;
    }

    private ClickableWidget getNewButtonElementButton() {
        return ButtonWidget.builder(
                        Text.literal("Create Chat Notification"),
                        (button) -> {
                            String id = "Custom Chat Notification #" + UUID.randomUUID();

                            CustomChatNotificationDataHandler.instance().createNewChatCustomNotification(id);

                            ButtonListWidget.ButtonEntry buttonEntry = createChatNotificationEntry(id);

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
                            if(selectedChatNotificationId != null) {
                                CustomChatNotificationDataHandler.instance().deleteCustomChatNotification(selectedChatNotificationId);
                                ChatHandler.instance().initChatTrigger();

                                ButtonListWidget.ButtonEntry entry = buttonEntryMap.get(selectedChatNotificationId);

                                buttonList.removeEntry(entry);
                                buttonEntryMap.remove(selectedChatNotificationId);

                                selectedChatNotificationId = null;
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
                                Triplet<String, String, Integer> data = gson.fromJson(json, TypeToken.getParameterized(Triplet.class, String.class, String.class, Integer.class).getType());

                                if(data.value3() > FishOnMCExtras.CHAT_NOTIFICATION_VERSION) {
                                    SystemToast.add(minecraftClient.getToastManager(),
                                            SystemToast.Type.PERIODIC_NOTIFICATION,
                                            Text.literal("Fish On Extras Rebirth"),
                                            Text.literal("Could not Import. Imported Chat Notification is made on a newer version"));
                                    return;
                                }

                                if(CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.containsKey(data.value1())) {
                                    data = Triplet.of(data.value1() + " (Duplicate)", data.value2(), data.value3());
                                }

                                String id = data.value1();

                                CustomChatNotificationDataHandler.instance().createNewChatCustomNotification(data.value1(), data.value2());

                                ButtonListWidget.ButtonEntry buttonEntry = createChatNotificationEntry(id);

                                buttonList.addEntry(buttonEntry);
                                buttonEntryMap.put(id, buttonEntry);

                                SystemToast.add(minecraftClient.getToastManager(),
                                        SystemToast.Type.PERIODIC_NOTIFICATION,
                                        Text.literal("Fish On Extras Rebirth"),
                                        Text.literal("Imported Chat Notification"));
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
                            if(selectedChatNotificationId != null) {
                                try {
                                    Triplet<String, String, Integer> dataButton = Triplet.of(
                                            selectedChatNotificationId,
                                            stringTextField.getText(),
                                            FishOnMCExtras.CHAT_NOTIFICATION_VERSION
                                    );

                                    String rawData = Base64.getEncoder().encodeToString(
                                            TextHelper.compress(new GsonBuilder().create().toJson(dataButton))
                                    );

                                    String dataToCopy = "**Custom Chat Notification: **" + selectedChatNotificationId + "\n" +
                                            "```\n" +
                                            rawData + "\n" +
                                            "```\n" +
                                            "-# Using Chat Notification version: " + "`v" + FishOnMCExtras.CHAT_NOTIFICATION_VERSION + "`";

                                    minecraftClient.keyboard.setClipboard(dataToCopy);

                                    SystemToast.add(minecraftClient.getToastManager(),
                                            SystemToast.Type.PERIODIC_NOTIFICATION,
                                            Text.literal("Fish On Extras Rebirth"),
                                            Text.literal("Exported Chat Notification on your clipboard"));
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
                "Custom Chat Notifications"
        );

        CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.forEach((name, text) -> {
            ButtonListWidget.ButtonEntry buttonEntry = createChatNotificationEntry(name);

            buttonList.addEntry(buttonEntry);
            buttonEntryMap.put(name, buttonEntry);
        });

        return buttonList;
    }

    private ButtonWidget saveBackButton() {
        return ButtonWidget.builder(Text.literal("Save and Return"), button -> {
            if(selectedChatNotificationId != null) {
                if(nameTextField.getText().isBlank()) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Chat Notification name is empty"));

                    return;
                }

                if(!Objects.equals(selectedChatNotificationId, nameTextField.getText())
                        && CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.containsKey(nameTextField.getText())
                ) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Chat Notification name already exist"));

                    return;
                }

                CustomChatNotificationDataHandler.instance().updateChatNotification(selectedChatNotificationId, nameTextField.getText(), stringTextField.getText());
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

    private ButtonListWidget.ButtonEntry createChatNotificationEntry(String id) {
        return new ButtonListWidget.ButtonEntry(
                ButtonWidget.builder(
                        Text.literal(id),
                        button -> {
                            selectedChatNotificationId = id;
                            stringField = CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.get(id);
                            this.setFields();
                        }
                ).width(BUTTON_WIDTH).build()
        );
    }

    private void setFields() {
        this.header = Text.literal(selectedChatNotificationId);
        nameTextField.setText(selectedChatNotificationId);
        nameTextField.setPlaceholder(Text.literal(selectedChatNotificationId));

        if(selectedChatNotificationId != null) {
            stringTextField.setText(stringField);
            stringTextField.setPlaceholder(Text.literal(stringField));
        }
    }

    private void resetFields() {
        this.header = Text.literal("No Chat Notification Selected");

        nameTextField.setText("");
        nameTextField.setPlaceholder(Text.literal(""));


        stringTextField.setText("");
        stringTextField.setPlaceholder(Text.literal(""));


        stringField = "";
        selectedChatNotificationId = null;
    }

    private ClickableWidget wikiButton() {
        return ButtonWidget.builder(Text.literal("Wiki"), button -> {
                    String url = "https://github.com/DannyPX/FishOnMC-Extras-R/wiki/Placeholders";

                    minecraftClient.setScreen(new ConfirmLinkScreen((confirmed) -> {
                        if (confirmed) {
                            Util.getOperatingSystem().open(url);
                        }

                        minecraftClient.setScreen(null);
                    }, url, true));
                })
                .position(PADDING_HALF + (BUTTON_WIDTH + PADDING * 2) + PADDING_HALF + BUTTON_WIDTH / 2, height - PADDING_HALF - BUTTON_HEIGHT)
                .size(BUTTON_WIDTH / 4, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Wiki to Placeholders")))
                .build();
    }

    @Override
    public void close() {
        this.minecraftClient.setScreen(this.parentScreen);
    }
    //endregion
}
