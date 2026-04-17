package dannypx.foe.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.store.CustomButtonDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.screens.interfaces.ScreenConstants;
import dannypx.foe.screens.widget.ButtonListWidget;
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

public class CustomButtonMakerScreen extends Screen implements ScreenConstants {
    //region Fields
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private final Screen parentScreen;
    private final String screenId;

    private ButtonListWidget buttonList;
    private Map<String, ButtonListWidget.ButtonEntry> buttonEntryMap = new HashMap<>();
    private String selectedButtonId;
    private CustomButtonDataHandler.CustomButton selectedButton;


    private Text header;
    private final int widgetHeight = 20;

    private TextFieldWidget nameTextField;
    private CheckboxWidget showButtonCheckBox;
    private final int sideWidth = 100;
    private TextFieldWidget descriptionTextField;
    private TextFieldWidget actionTextField;
    private TextFieldWidget iconTextField;
    //endregion

    //region Methods
    public CustomButtonMakerScreen(Screen parent, String screenId) {
        super(Text.literal("Custom Button Maker Screen"));
        this.parentScreen = parent;
        this.screenId = screenId;
    }

    @Override
    protected void init() {
        super.init();
        CustomButtonDataHandler.instance().init(this.screenId);
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
        if(descriptionTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Can be empty").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(actionTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Must start with \"/\"").formatted(Formatting.GRAY)
            ), mouseX, mouseY);
        }

        if(iconTextField.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, List.of(
                    Text.literal("Must be a single character or is an item").formatted(Formatting.GRAY),
                    Text.literal("using one of the following formats: ").formatted(Formatting.GRAY),
                    Text.literal("\"minecraft:<id>\"").formatted(Formatting.GOLD),
                    Text.literal("\"minecraft:<id>[<componentData>]\"").formatted(Formatting.GOLD)

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
                Text.literal("Description"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 2,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Command"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - textRenderer.fontHeight / 2 + (widgetHeight + PADDING) * 3,
                0xFFFFFF,
                true
        );

        context.drawText(textRenderer,
                Text.literal("Icon"),
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
        widgets.add(getShowButtonCheckBox());
        widgets.add(getDescriptionTextField());
        widgets.add(getActionTextField());
        widgets.add(getIconTextField());

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
            if(selectedButtonId != null) {
                nameTextField.setPlaceholder(Text.literal(s));
            }
        });

        return nameTextField;
    }

    private ClickableWidget getShowButtonCheckBox() {
        showButtonCheckBox = CheckboxWidget.builder(
                        Text.literal("Show Button"),
                        textRenderer
                )
                .pos(minecraftClient.getWindow().getScaledWidth() - PADDING - sideWidth
                        , PADDING + widgetHeight + PADDING)
                .checked(true)
                .callback((checkbox, checked) -> {})
                .build();
        return showButtonCheckBox;
    }

    private ClickableWidget getDescriptionTextField() {
        descriptionTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 2,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        descriptionTextField.setMaxLength(Integer.MAX_VALUE);

        descriptionTextField.setChangedListener(s -> {
            if(selectedButtonId != null) {
                descriptionTextField.setPlaceholder(Text.literal(s));
            }
        });

        return descriptionTextField;
    }

    private ClickableWidget getActionTextField() {
        actionTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 3,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        actionTextField.setMaxLength(Integer.MAX_VALUE);

        actionTextField.setChangedListener(s -> {
            if(selectedButtonId != null) {
                actionTextField.setPlaceholder(Text.literal(s));
            }
        });

        return actionTextField;
    }

    private ClickableWidget getIconTextField() {
        iconTextField = new TextFieldWidget(
                textRenderer,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 4,
                minecraftClient.getWindow().getScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Text.empty()
        );
        iconTextField.setMaxLength(Integer.MAX_VALUE);

        iconTextField.setChangedListener(s -> {
            if(selectedButtonId != null) {
                iconTextField.setPlaceholder(Text.literal(s));
            }
        });

        return iconTextField;
    }

    private ClickableWidget getNewButtonElementButton() {
        return ButtonWidget.builder(
                        Text.literal("Create Button"),
                        (button) -> {
                            String id = "Custom Button #" + UUID.randomUUID();

                            CustomButtonDataHandler.instance().createNewButton(screenId, id);

                            ButtonListWidget.ButtonEntry buttonEntry = createButtonEntry(id);

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
                            if(selectedButtonId != null) {
                                CustomButtonDataHandler.instance().deleteButton(screenId, selectedButtonId);

                                ButtonListWidget.ButtonEntry entry = buttonEntryMap.get(selectedButtonId);

                                buttonList.removeEntry(entry);
                                buttonEntryMap.remove(selectedButtonId);

                                selectedButtonId = null;
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
                                Pair<CustomButtonDataHandler.CustomButton, Integer> data = gson.fromJson(json, TypeToken.getParameterized(Pair.class, CustomButtonDataHandler.CustomButton.class, Integer.class).getType());

                                if(data.value2() > FishOnMCExtras.BUTTON_VERSION) {
                                    SystemToast.add(minecraftClient.getToastManager(),
                                            SystemToast.Type.PERIODIC_NOTIFICATION,
                                            Text.literal("Fish On Extras Rebirth"),
                                            Text.literal("Could not Import. Imported Button is made on a newer version"));

                                    return;
                                }

                                if(CustomButtonDataHandler.instance().getCustomButtonData().buttonList.getOrDefault(screenId, Pair.of(new ArrayList<>(), false)).value1().stream().anyMatch(b -> Objects.equals(b.name, data.value1().name))) {
                                    data.value1().name = data.value1().name + " (Duplicate)";
                                }

                                String id = data.value1().name;

                                CustomButtonDataHandler.instance().createNewButton(screenId, data.value1());

                                ButtonListWidget.ButtonEntry buttonEntry = createButtonEntry(id);

                                buttonList.addEntry(buttonEntry);
                                buttonEntryMap.put(id, buttonEntry);

                                SystemToast.add(minecraftClient.getToastManager(),
                                        SystemToast.Type.PERIODIC_NOTIFICATION,
                                        Text.literal("Fish On Extras Rebirth"),
                                        Text.literal("Imported Button"));
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
                            if(selectedButtonId != null) {
                                try {
                                    Pair<CustomButtonDataHandler.CustomButton, Integer> dataButton = Pair.of(
                                            selectedButton,
                                            FishOnMCExtras.BUTTON_VERSION
                                    );

                                    String rawData = Base64.getEncoder().encodeToString(
                                            TextHelper.compress(new GsonBuilder().create().toJson(dataButton))
                                    );

                                    String dataToCopy = "**Custom Button: **" + selectedButtonId + "\n" +
                                            "```\n" +
                                            rawData + "\n" +
                                            "```\n" +
                                            "-# Using Button version: " + "`v" + FishOnMCExtras.BUTTON_VERSION + "`";

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
                "Custom Buttons"
        );

        CustomButtonDataHandler.instance().getCustomButtonData().buttonList.getOrDefault(screenId, Pair.of(new ArrayList<>(), false)).value1().forEach((button) -> {
            ButtonListWidget.ButtonEntry buttonEntry = createButtonEntry(button.name);

            buttonList.addEntry(buttonEntry);
            buttonEntryMap.put(button.name, buttonEntry);
        });

        return buttonList;
    }

    private ButtonWidget saveBackButton() {
        return ButtonWidget.builder(Text.literal("Save and Return"), button -> {
            if(selectedButtonId != null) {
                if(nameTextField.getText().isBlank()) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Button name is empty"));

                    return;
                }

                if(CustomButtonDataHandler.instance().getCustomButtonData().buttonList.getOrDefault(screenId, Pair.of(new ArrayList<>(), false)).value1().stream().anyMatch(b -> Objects.equals(b.name, nameTextField.getText()))
                        && !Objects.equals(selectedButton.name, nameTextField.getText())
                ) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Button name already exist"));

                    return;
                }

                if(!actionTextField.getText().startsWith("/")) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Command must start with /"));

                    return;
                }

                Pattern iconPattern = Pattern.compile("^(?:([a-z_]+:[a-z_]+)(?:\\[(.*)\\])?|(.))$");
                if(!iconPattern.matcher(iconTextField.getText()).matches()) {
                    SystemToast.add(minecraftClient.getToastManager(),
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Fish On Extras Rebirth"),
                            Text.literal("Icon is not right format"));

                    return;
                }

                CustomButtonDataHandler.instance().updateButton(screenId, selectedButton,
                        nameTextField.getText(),
                        descriptionTextField.getText(),
                        actionTextField.getText(),
                        iconTextField.getText(),
                        showButtonCheckBox.isChecked());
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

    private ButtonListWidget.ButtonEntry createButtonEntry(String id) {
        return new ButtonListWidget.ButtonEntry(
                ButtonWidget.builder(
                        Text.literal(TextHelper.parseLegacyWithStyle(id.replace("&", "§")).value1().getString()),
                        button -> {
                            selectedButton = CustomButtonDataHandler.instance().getCustomButtonData().buttonList.getOrDefault(screenId, Pair.of(new ArrayList<>(), false)).value1().stream().filter(buttonObject -> Objects.equals(buttonObject.name, id)).findAny().orElse(null);

                            if(selectedButton != null) {
                                selectedButtonId = id;
                                this.setFields();
                            }
                        }
                ).width(BUTTON_WIDTH / 4 * 3).build(),
                ButtonWidget.builder(
                                Text.literal("Add"),
                                button -> CodeExecuterHandler.runLater(1, "createButtonEntry > Add", () -> {
                                    String newId = "Custom Hud #" + UUID.randomUUID();

                                    int pos = buttonList.children().indexOf(buttonEntryMap.get(id));

                                    CustomButtonDataHandler.instance().createNewButton(screenId, newId, pos);

                                    ButtonListWidget.ButtonEntry buttonEntry = createButtonEntry(newId);

                                    buttonList.addEntry(buttonEntry, pos);
                                    buttonEntryMap.put(newId, buttonEntry);
                                }))
                        .width(25)
                        .tooltip(Tooltip.of(Text.literal("Add new button")))
                        .build(),
                ButtonWidget.builder(
                                Text.literal("⏶"),
                                button -> CodeExecuterHandler.runLater(1, "createButtonEntry > Move Up", () -> {
                                    int pos = buttonList.children().indexOf(buttonEntryMap.get(id));

                                    if(pos > 0) {
                                        CustomButtonDataHandler.instance().swapUp(screenId, pos);

                                        buttonList.swapUp(pos);
                                    }
                                }))
                        .size(25, 10)
                        .tooltip(Tooltip.of(Text.literal("Move button up")))
                        .build(),
                ButtonWidget.builder(
                                Text.literal("⏷"),
                                button -> CodeExecuterHandler.runLater(1, "createButtonEntry > Move Down", () -> {
                                    int pos = buttonList.children().indexOf(buttonEntryMap.get(id));

                                    if(pos < buttonList.children().size() - 1) {
                                        CustomButtonDataHandler.instance().swapDown(screenId, pos);

                                        buttonList.swapDown(pos);
                                    }
                                }))
                        .size(25, 10)
                        .tooltip(Tooltip.of(Text.literal("Move button down")))
                        .build()
        );
    }

    private void setFields() {
        this.header = TextHelper.parseLegacyWithStyle(selectedButtonId.replace("&", "§")).value1();
        nameTextField.setText(selectedButtonId);
        nameTextField.setPlaceholder(Text.literal(selectedButtonId));

        if(selectedButton != null) {
            if(selectedButton.showButton != showButtonCheckBox.isChecked()) {
                showButtonCheckBox.onPress();
            }

            descriptionTextField.setText(selectedButton.description);
            descriptionTextField.setPlaceholder(Text.literal(selectedButton.description));

            actionTextField.setText(selectedButton.action);
            actionTextField.setPlaceholder(Text.literal(selectedButton.action));

            iconTextField.setText(selectedButton.icon);
            iconTextField.setPlaceholder(Text.literal(selectedButton.icon));
        }
    }

    private void resetFields() {
        this.header = Text.literal("No Button Selected");

        nameTextField.setText("");
        nameTextField.setPlaceholder(Text.literal(""));

        if(showButtonCheckBox.isChecked()) {
            showButtonCheckBox.onPress();
        }

        descriptionTextField.setText("");
        descriptionTextField.setPlaceholder(Text.literal(""));

        actionTextField.setText("");
        actionTextField.setPlaceholder(Text.literal(""));

        iconTextField.setText("");
        iconTextField.setPlaceholder(Text.literal(""));

        selectedButton = null;
        selectedButtonId = null;
    }

    @Override
    public void close() {
        this.minecraftClient.setScreen(this.parentScreen);
    }
    //endregion
}
