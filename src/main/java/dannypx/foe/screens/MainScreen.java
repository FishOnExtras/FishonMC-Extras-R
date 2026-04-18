package dannypx.foe.screens;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.config.Configs;
import dannypx.foe.screens.debug.DebugHandlerScreen;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends DefaultModScreen {
    //region Fields
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private static final Identifier ICON_TEXTURE = Identifier.of(FishOnMCExtras.MOD_ID, "elements/icon");
    //endregion

    //region Methods
    public MainScreen(Screen parent) {
        super(parent, Text.literal("Main Screen"));
    }

    @Override
    protected void init() {
        super.init();
        this.renderWidgets();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);

        int screenWidth = minecraftClient.getWindow().getScaledWidth();
        int screenHeight = minecraftClient.getWindow().getScaledHeight();

        int size = 200;

        drawContext.drawGuiTexture(RenderLayer::getGuiTextured,
                ICON_TEXTURE,
                screenWidth / 2 - size / 2, screenHeight / 2 - size + 32,
                size, size
        );

        Text hudText = Text.literal("Creator Settings");
        drawContext.drawText(textRenderer, hudText, width / 2 - textRenderer.getWidth(hudText) / 2, height / 2 - PADDING_QUART - textRenderer.fontHeight, 0xFFFFFF, true);

        Text configText = Text.literal("Configuration");
        drawContext.drawText(textRenderer, configText, width / 2 - textRenderer.getWidth(configText) / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 3 + BUTTON_HEIGHT + PADDING, 0xFFFFFF, true);

        //Versions
        drawContext.drawText(textRenderer, Text.literal("Mod Version: v" + FishOnMCExtras.VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - textRenderer.fontHeight - PADDING_QUART, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal("HUD Version: v" + FishOnMCExtras.HUD_VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - (textRenderer.fontHeight + PADDING_QUART) * 2, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal("Chat Trigger Version: v" + FishOnMCExtras.CHAT_TRIGGER_VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - (textRenderer.fontHeight + PADDING_QUART) * 3, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal("Timer Version: v" + FishOnMCExtras.TIMER_VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - (textRenderer.fontHeight + PADDING_QUART) * 4, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal("Notification Version: v" + FishOnMCExtras.NOTIFICATION_VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - (textRenderer.fontHeight + PADDING_QUART) * 5, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal("Button Version: v" + FishOnMCExtras.BUTTON_VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - (textRenderer.fontHeight + PADDING_QUART) * 6, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal("Chat Notification Version: v" + FishOnMCExtras.CHAT_NOTIFICATION_VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - (textRenderer.fontHeight + PADDING_QUART) * 7, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal("Event Trigger Version: v" + FishOnMCExtras.EVENT_TRIGGER_VERSION).formatted(Formatting.DARK_GRAY), PADDING_QUART, height - (textRenderer.fontHeight + PADDING_QUART) * 8, 0xFFFFFF, true);
    }

    private void renderWidgets() {
        List<ClickableWidget> widgets = new ArrayList<>();

        widgets.add(customHudButton());
        widgets.add(moveHudButton());
        widgets.add(customChatTriggerButton());
        widgets.add(customTimerButton());
        widgets.add(customNotificationButton());
        widgets.add(customChatNotificationButton());
        widgets.add(customEventTriggerButton());
        widgets.add(configButton());
        widgets.add(controlsButton());

        widgets.forEach(this::addDrawableChild);
    }

    private ButtonWidget customHudButton() {
        return ButtonWidget.builder(Text.literal("Create HUDs"), button ->
                        minecraftClient.setScreen(new CustomHudMakerScreen(minecraftClient.currentScreen)))
                .position(width / 2 - BUTTON_WIDTH / 2, height / 2)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Custom HUD Creator Screen")))
                .build();
    }

    private ButtonWidget moveHudButton() {
        return ButtonWidget.builder(Text.literal("Move HUDs"), button ->
                        minecraftClient.setScreen(new MoveElementScreen(minecraftClient.currentScreen)))
                .position(width / 2 + PADDING_HALF, height / 2)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Move HUD Elements Screen")))
                .build();
    }

    private ButtonWidget customChatTriggerButton() {
        return ButtonWidget.builder(Text.literal("Create Chat Triggers"), button ->
                        minecraftClient.setScreen(new CustomChatTriggerMakerScreen(minecraftClient.currentScreen)))
                .position(width / 2 - BUTTON_WIDTH / 2, height / 2 + BUTTON_HEIGHT + PADDING_HALF)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Custom Chat Trigger Creator Screen")))
                .build();
    }

    private ButtonWidget customTimerButton() {
        return ButtonWidget.builder(Text.literal("Create Timers"), button ->
                        minecraftClient.setScreen(new CustomTimerMakerScreen(minecraftClient.currentScreen)))
                .position(width / 2 + PADDING_HALF, height / 2 + BUTTON_HEIGHT + PADDING_HALF)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Custom Notification Creator Screen")))
                .build();
    }

    private ButtonWidget customNotificationButton() {
        return ButtonWidget.builder(Text.literal("Create Notifications"), button ->
                        minecraftClient.setScreen(new CustomNotificationMakerScreen(minecraftClient.currentScreen)))
                .position(width / 2 - BUTTON_WIDTH / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Custom Notification Creator Screen")))
                .build();
    }

    private ButtonWidget customChatNotificationButton() {
        return ButtonWidget.builder(Text.literal("Create Chat Notifications"), button ->
                        minecraftClient.setScreen(new CustomChatNotificationMakerScreen(minecraftClient.currentScreen)))
                .position(width / 2 + PADDING_HALF, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Custom Chat Notification Creator Screen")))
                .build();
    }

    private ButtonWidget customEventTriggerButton() {
        return ButtonWidget.builder(Text.literal("Create Event Triggers"), button ->
                        minecraftClient.setScreen(new CustomEventTriggerMakerScreen(minecraftClient.currentScreen)))
                .position(width / 2 - BUTTON_WIDTH / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 3)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Custom Event Trigger Creator Screen")))
                .build();
    }

    private ButtonWidget configButton() {
        return ButtonWidget.builder(Text.literal("Config Screen"), button ->
                        ConfigApiJava.INSTANCE.openScreen(FishOnMCExtras.MOD_ID))
                .position(width / 2 - BUTTON_WIDTH / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 3 + BUTTON_HEIGHT + PADDING + textRenderer.fontHeight + PADDING_QUART)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Config Screen")))
                .build();
    }

    private ButtonWidget controlsButton() {
        return ButtonWidget.builder(Text.literal("Controls"), button ->
                        ConfigApiJava.INSTANCE.openScreen(Configs.keyBindConfig.translationKey()))
                .position(width / 2 + PADDING_HALF, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 3 + BUTTON_HEIGHT + PADDING + textRenderer.fontHeight + PADDING_QUART)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Open Controls Config")))
                .build();
    }
    //endregion
}
