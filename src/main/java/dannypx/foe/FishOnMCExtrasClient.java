package dannypx.foe;

import dannypx.foe.command.CommandRegistry;
import dannypx.foe.entity.FishingBobberEntityModel;
import dannypx.foe.handler.fetch.*;
import dannypx.foe.handler.logic.*;
import dannypx.foe.handler.renderer.*;
import dannypx.foe.handler.store.*;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.config.Configs;
import dannypx.foe.screens.debug.DebugHandlerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

public class FishOnMCExtrasClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        this.onInit();

        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        ScreenEvents.BEFORE_INIT.register(this::onBeforeInitScreen);
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onLeave);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        ClientReceiveMessageEvents.GAME.register(this::receiveGameMessage);
        ClientReceiveMessageEvents.MODIFY_GAME.register(this::modifyGameMessage);
        ClientSendMessageEvents.MODIFY_CHAT.register(this::modifyChatMessage);
        HudLayerRegistrationCallback.EVENT.register(this::onHudRenderCallback);
        ScreenEvents.AFTER_INIT.register(this::onAfterInitScreen);
        UseItemCallback.EVENT.register(this::onUseItem);
        ItemTooltipCallback.EVENT.register(this::onItemTooltip);
    }


    private void onItemTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> texts) {
        TooltipHandler.instance().fetchTooltip(itemStack, tooltipContext, tooltipType, texts);
    }

    private void onClientStarted(MinecraftClient minecraftClient) {
        if(minecraftClient.options.getGuiScale().getValue() == 0) {
            minecraftClient.options.getGuiScale().setValue(3);
            minecraftClient.options.write();
            minecraftClient.onResolutionChanged();
        }
    }

    private void receiveGameMessage(Text text, boolean overlay) {
        ChatHandler.instance().onReceiveMessage(text);
    }

    private Text modifyGameMessage(Text text, boolean over) {
        return ChatHandler.instance().onModifyMessage(text);
    }

    private String modifyChatMessage(String text) {
        return ChatHandler.instance().onModifyChatMessage(text);
    }

    private ActionResult onUseItem(PlayerEntity player, World world, Hand hand) {
        return ActionResult.PASS;
    }

    private void onAfterInitScreen(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        SearchHandler.instance().setFocused(false);
        SearchHandler.instance().setOnScreen(false);

        PersonalVaultScreenRenderHandler.instance().setOnScreen(false);
        AuctionHouseScreenRenderHandler.instance().setOnScreen(false);
        PresetsScreenRenderHandler.instance().setOnScreen(false);

        if(screen instanceof InventoryScreen) {
            InventoryScreenRenderHandler.instance().init(screen);
            ScreenEvents.afterRender(screen).register(InventoryScreenRenderHandler.instance()::render);
            ScreenMouseEvents.afterMouseScroll(screen).register(InventoryScreenRenderHandler.instance()::onMouseScrolled);
        } else if(screen instanceof GenericContainerScreen genericContainerScreen) {
            GenericContainerScreenHandler.instance().init(genericContainerScreen);
            ScreenEvents.afterRender(screen).register(GenericContainerScreenHandler.instance()::render);
        } else if(screen instanceof ChatScreen) {
            ScreenEvents.afterRender(screen).register(ChatScreenRenderHandler.instance()::render);
        }

        ScreenEvents.remove(screen).register(this::onRemoveScreen);
    }

    private void onRemoveScreen(Screen screen) {
        InventoryHandler.instance().trackFishOffSide();
    }

    private void onBeforeInitScreen(MinecraftClient minecraftClient, Screen screen, int scaledWidth, int scaledHeight) {
        ScreenMouseEvents.afterMouseScroll(screen).register(this::afterMouseScroll);
    }

    private void afterMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        PersonalVaultScreenRenderHandler.instance().checkMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
        AuctionHouseScreenRenderHandler.instance().checkMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
        PresetsScreenRenderHandler.instance().checkMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void onHudRenderCallback(LayeredDrawerWrapper layeredDrawerWrapper) {
        HudRenderHandler.instance().hudRenderCallback(layeredDrawerWrapper);
    }

    private void onInit() {
        this.registerEntityModels();
        CodeExecuterHandler.instance().init();
        CommandRegistry.init();
    }

    private void onLeave(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
        ConnectionHandler.instance().onLeave();
        LoadingHandler.instance().onLeave();

        InventoryHandler.instance().onLeave();

    }

    private void onJoin(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient minecraftClient) {
        ConnectionHandler.instance().init();
        if(ConnectionHandler.instance().isOnServer()) {
            ProfileDataHandler.instance().init();
            StatsDataHandler.instance().init();
            ConstantDataHandler.instance().init();
            QuestDataHandler.instance().init();
            CrewDataHandler.instance().init();
            CustomHudDataHandler.instance().init();
            CustomButtonDataHandler.instance().init();
            CustomNotificationDataHandler.instance().init();
            CustomChatTriggerDataHandler.instance().init();
            CustomChatNotificationDataHandler.instance().init();
            CustomTimerDataHandler.instance().init();
            CustomEventTriggerDataHandler.instance().init();

            ScoreboardHandler.instance().init();
            CrewHandler.instance().init();

            DataFileHandler.instance().init();
            LoadingHandler.instance().init();

            ChatHandler.instance().init();
            NotifierHandler.instance().init();
            TimerHandler.instance().init();
        }
    }

    private void onEndClientTick(MinecraftClient minecraftClient) {
        if(!LoadingHandler.instance().isError()
                && minecraftClient.getCurrentServerEntry() != null
                // Check if on server before ticking
                && ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
        ) {
            // Check if done loading
            if(LoadingHandler.instance().isLoadingDone()) {
                // Fetch
                if(Configs.handlerConfig.tabHandler.get()) TabHandler.instance().tick();
                if(Configs.handlerConfig.scoreboardHandler.get()) ScoreboardHandler.instance().tick();
                if(Configs.handlerConfig.clientPlayerHandler.get()) ClientPlayerHandler.instance().tick();
                if(Configs.handlerConfig.bossBarHandler.get()) BossBarHandler.instance().tick();
                if(Configs.handlerConfig.inventoryHandler.get()) InventoryHandler.instance().tick();
                if(Configs.handlerConfig.networkHandler.get()) NetworkHandler.instance().tick();

                // IO
                if(Configs.handlerConfig.dataFileHandler.get()) DataFileHandler.instance().tick();

                // Logic
                if(Configs.handlerConfig.keyBindHandler.get()) KeyBindHandler.instance().tick();
                if(Configs.handlerConfig.catchingHandler.get()) CatchingHandler.instance().tick();
                if(Configs.handlerConfig.rayCastHandler.get()) RayCastHandler.instance().tick();
                if(Configs.handlerConfig.notifierHandler.get()) NotifierHandler.instance().tick();
                if(Configs.handlerConfig.crewHandler.get()) CrewHandler.instance().tick();
                if(Configs.handlerConfig.lightHandler.get()) LightHandler.instance().tick();
                if(Configs.handlerConfig.timerHandler.get()) TimerHandler.instance().tick();

                // Renderer
                if(Configs.handlerConfig.hudRenderHandler.get()) HudRenderHandler.instance().tick();

            } else {
                if(Configs.handlerConfig.loadingHandler.get()) LoadingHandler.instance().tick();
            }
        }
    }

    private void registerEntityModels() {
        EntityModelLayerRegistry.registerModelLayer(FishingBobberEntityModel.MODEL_LAYER, FishingBobberEntityModel::generateModel);
    }
}
