package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.renderer.AuctionHouseScreenRenderHandler;
import dannypx.foe.handler.renderer.ChestScreenRenderHandler;
import dannypx.foe.handler.renderer.PersonalVaultScreenRenderHandler;
import dannypx.foe.handler.renderer.PresetsScreenRenderHandler;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Objects;

public class GenericContainerScreenHandler extends Handler {
    private static GenericContainerScreenHandler INSTANCE = new GenericContainerScreenHandler();

    public static GenericContainerScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new GenericContainerScreenHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private String lastContainerScreen = "";

    public String getLastContainerScreen() {
        return lastContainerScreen;
    }

    public static final String QUEST_SCREEN_CONTAINER = "\uEEE4\uD539";
    public static final String STATS_SCREEN_CONTAINER = "\uEEE4\uD532";
    public static final String AUCTION_HOUSE_SCREEN_CONTAINER = "\uEEE4\uD543";
    public static final String PERSONAL_VAULT_SCREEN_CONTAINER = "Personal Vault #";
    public static final String ARMOR_MENU_SCREEN_CONTAINER = "Armor Menu";
    public static final String STORAGE_SCREEN_CONTAINER = " ";
    public static final String GENERIC_SCREEN_CONTAINER = "\uEEE4\uD552";
    public static final String PRESETS_SCREEN_CONTAINER = "Presets\uEEE6\uEEE5\uD572";
    //endregion

    //region Methods
    public void init(GenericContainerScreen genericContainerScreen) {
        if(!Configs.handlerConfig.genericContainerScreenHandler.get()) {
            return;
        }

        this.checkIsOfTitle(genericContainerScreen);
    }

    private void checkIsOfTitle(GenericContainerScreen genericContainerScreen) {
        this.lastContainerScreen = genericContainerScreen.getTitle().getString();

        if (Objects.equals(genericContainerScreen.getTitle().getString(), QUEST_SCREEN_CONTAINER)) {
            QuestScreenHandler.instance().checkQuests(genericContainerScreen.getScreenHandler());
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), STATS_SCREEN_CONTAINER)) {
            StatsScreenHandler.instance().checkStats(genericContainerScreen.getScreenHandler());
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), AUCTION_HOUSE_SCREEN_CONTAINER)) {
            AuctionHouseScreenRenderHandler.instance().init(genericContainerScreen);
        } else if (genericContainerScreen.getTitle().getString().startsWith(PERSONAL_VAULT_SCREEN_CONTAINER)) {
            PersonalVaultScreenRenderHandler.instance().init(genericContainerScreen);
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), STORAGE_SCREEN_CONTAINER)) {
            ChestScreenRenderHandler.instance().init(genericContainerScreen);
        } else if (genericContainerScreen.getTitle().getString().startsWith(ARMOR_MENU_SCREEN_CONTAINER)) {
            ArmorRollScreenHandler.instance().checkArmorRolls(genericContainerScreen.getScreenHandler());
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), PRESETS_SCREEN_CONTAINER)) {
            PresetsScreenRenderHandler.instance().init(genericContainerScreen);
        } else if(Objects.equals(genericContainerScreen.getTitle().getString(), GENERIC_SCREEN_CONTAINER)) {
            this.checkIsOfItem(genericContainerScreen);
        }
    }

    private void checkIsOfItem(GenericContainerScreen genericContainerScreen) {
        CodeExecuterHandler.runLater(2, "checkIsOfItem", () -> {
            net.minecraft.screen.GenericContainerScreenHandler genericContainerScreenHandler = genericContainerScreen.getScreenHandler();

            // Crew Info
            ItemStack crewInfoStack = genericContainerScreenHandler.getSlot(13).getStack();
            if(Objects.equals(crewInfoStack.getName().getString(), "Crew Info")) {
                CrewScreenHandler.instance().checkCrewInfo(genericContainerScreenHandler);
            }
        });
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "lastContainerScreen", Pair.of(Text.literal(getLastContainerScreen()), Text.empty())
        );
    }

    public void render(Screen screen, DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        if(screen instanceof GenericContainerScreen genericContainerScreen) {
            if (Objects.equals(genericContainerScreen.getTitle().getString(), AUCTION_HOUSE_SCREEN_CONTAINER)) {
                AuctionHouseScreenRenderHandler.instance().renderButtonHelp(drawContext, true, true);
            } else if (genericContainerScreen.getTitle().getString().startsWith(PERSONAL_VAULT_SCREEN_CONTAINER)) {
                PersonalVaultScreenRenderHandler.instance().renderButtonHelp(drawContext, true, true);
            } else if (Objects.equals(genericContainerScreen.getTitle().getString(), STORAGE_SCREEN_CONTAINER)) {
                ChestScreenRenderHandler.instance().renderButtonHelp(drawContext, true, false);
            } else if (Objects.equals(genericContainerScreen.getTitle().getString(), PRESETS_SCREEN_CONTAINER)) {
                PresetsScreenRenderHandler.instance().renderButtonHelp(drawContext, true, true);
            }
        }
    }
    //endregion
}
