package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.TitleHandler;
import dannypx.foe.handler.store.ConstantDataHandler;
import dannypx.foe.handler.store.QuestDataHandler;
import dannypx.foe.handler.store.StatsDataHandler;
import dannypx.foe.item.FishNbtObject;
import dannypx.foe.item.NbtObject;
import dannypx.foe.item.ValidateItem;
import dannypx.foe.type.custom_text.CustomTextValue;
import dannypx.foe.type.custom_text.StringValue;
import dannypx.foe.type.custom_text.TextValue;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import dannypx.foe.type.tuple.Triplet;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class CatchingHandler extends Handler {
    private static CatchingHandler INSTANCE = new CatchingHandler();

    public static CatchingHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CatchingHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private long startScanTime = 0L;
    private boolean scanDone = true;
    private String fishNameToFind = "";

    private FishNbtObject lastCaughtFish = FishNbtObject.empty();
    // Rarity Variant Size
    private Triplet<Pair<String, Integer>, Pair<String, Integer>, Pair<String, Integer>> lastDataFish = null;

    public boolean isScanDone() {
        return scanDone;
    }

    public Pair<Boolean, CustomTextValue> getCatch(String[] params) {
        if(params.length > 2) {
            Pattern fieldPattern = Pattern.compile("^(last_caught)$");

            if(fieldPattern.matcher(params[0]).matches()) {
                return switch(params[0]) {
                    case "last_caught" -> switch (params[1]) {
                        case "fish" -> {
                            if(lastCaughtFish.getItemStack() != ItemStack.EMPTY && lastDataFish != null) {
                                yield switch (params[2]) {
                                    case "name" -> PlaceholderHandler.getTextValue(new TextValue(lastCaughtFish.getName()));
                                    case "rarity", "variant", "size" -> {
                                        Pair<String, Integer> drystreakData = null;
                                        Text icon = null;

                                        switch (params[2]) {
                                            case "rarity" -> {
                                                drystreakData = lastDataFish.value1();
                                                icon = lastCaughtFish.getRarityText();
                                            }
                                            case "variant" -> {
                                                drystreakData = lastDataFish.value2();
                                                icon = lastCaughtFish.getVariantText();
                                            }
                                            case "size" -> {
                                                drystreakData = lastDataFish.value3();
                                                icon = lastCaughtFish.getFishSizeText();
                                            }
                                        }

                                        if(icon != null
                                                && params.length == 4
                                        ) {
                                            yield switch (params[3]) {
                                                case "name" -> PlaceholderHandler.getTextValue(new StringValue(drystreakData.value1()));
                                                case "icon" -> PlaceholderHandler.getTextValue(new TextValue(icon), true);
                                                case "last_drystreak" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(drystreakData.value2())));
                                                default -> PlaceholderHandler.noResult();
                                            };
                                        }
                                        yield PlaceholderHandler.noResult();
                                    }
                                    default -> PlaceholderHandler.getNbtTextValue(lastCaughtFish, params[2]);
                                };
                            }
                            yield PlaceholderHandler.noResult();
                        }
                        default -> PlaceholderHandler.noResult();
                    };
                    default -> PlaceholderHandler.noResult();
                };
            }
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void tick() {
        this.scanFish();
    }

    private void scanFish() {
        if(!scanDone && System.currentTimeMillis() < startScanTime + (Configs.handlerConfig.catchingStatusCooldown.get() * 1000L)) {
            Pair<Boolean, FishNbtObject> foundFish = this.findFish();
            if(foundFish.value1()) {
                InventoryHandler.instance().trackAllFish();

                // Store to Stats
                Triplet<Pair<String, Integer>, Pair<String, Integer>, Pair<String, Integer>> prevStats = StatsDataHandler.instance().setFish(foundFish.value2());
                NotifierHandler.instance().notifyFish(foundFish.value2(), prevStats.value1(), prevStats.value2(), prevStats.value3());

                QuestDataHandler.instance().setFish(foundFish.value2());
                LoggerHandler._debug("Found Fish: " + foundFish.value2().getName().getString());

                CodeExecuterHandler.runLater(Configs.handlerConfig.catchingItemsDelayCheck.get(), "scanFish > checkForCaughtItems", this::checkForCaughtItems);

                lastDataFish = prevStats;
                lastCaughtFish = foundFish.value2();
                CodeExecuterHandler.runLater(1, "scanFish > onCatch", EventHandler.instance()::onCatch);
                this.scanDone = true;
            }
        } else if (!scanDone && System.currentTimeMillis() > startScanTime + (Configs.handlerConfig.catchingStatusCooldown.get() * 1000L)){
            this.scanDone = true;
            LoggerHandler._debug("Did not find fish");
        }
    }

    private void checkForCaughtItems() {
        LoggerHandler._debug("Start finding items");
        LoggerHandler._debug("Start Time: " + System.currentTimeMillis());
        LoggerHandler._debug("Search Window: " + (Configs.handlerConfig.catchingItemsCheckWindow.get() + (System.currentTimeMillis() - startScanTime)));
        if(minecraftClient.player != null) {
            InventoryHandler.instance().getSnapshottedItems().stream()
                    .filter(item -> System.currentTimeMillis() - item.value1()
                            < Configs.handlerConfig.catchingItemsCheckWindow.get() + (System.currentTimeMillis() - startScanTime))
                    .toList().forEach(item -> scanItem(item.value2(), item.value3()));
        }
    }

    private void scanItem(ItemStack itemStack, int count) {
        Pair<Boolean, NbtObject> validatedItem = ValidateItem.isType(itemStack);

        if(validatedItem.value1()) {
            // Store to Stats
            StatsDataHandler.instance().setItem(validatedItem.value2(), count);

            LoggerHandler._debug("Found Item: " + itemStack.getName().getString(), itemStack);
        }
    }

    private Pair<Boolean, FishNbtObject> findFish() {
        FishNbtObject inventoryFish = this.findFishInInventory();
        if(inventoryFish != null) return Pair.of(inventoryFish);

        FishNbtObject worldFish = this.findFishInWorld();
        if(worldFish != null) return Pair.of(worldFish);

        return Pair.ofFalse(FishNbtObject.empty());
    }

    private FishNbtObject findFishInInventory() {
        AtomicReference<FishNbtObject> foundItemStack = new AtomicReference<>();

        if(minecraftClient.player != null) {
            minecraftClient.player.getInventory().main.forEach(itemStack -> {
                FishNbtObject validatedFish = validateFish(itemStack);
                if(validatedFish != null && foundItemStack.get() == null) foundItemStack.set(validatedFish);
            });
        }
        return foundItemStack.get();
    }

    private FishNbtObject findFishInWorld() {
        AtomicReference<FishNbtObject> foundItemStack = new AtomicReference<>();

        if(minecraftClient.player != null) {
            ClientWorld world = minecraftClient.player.clientWorld;
            Box searchBox = minecraftClient.player.getBoundingBox().expand(10d);

            List<ItemEntity> itemEntities = world.getEntitiesByClass(
                    ItemEntity.class,
                    searchBox,
                    itemEntity -> {
                        ItemStack itemStack = itemEntity.getStack();
                        FishNbtObject validatedFish = validateFish(itemStack);
                        return validatedFish != null;
                    }
            );

            if(!itemEntities.isEmpty() && foundItemStack.get() == null) {
                foundItemStack.set(ValidateItem.isFish(itemEntities.getFirst().getStack()).value2());
            }
        }
        return foundItemStack.get();
    }

    private FishNbtObject validateFish(ItemStack itemStack) {
        if(!itemStack.isEmpty()) {
            Pair<Boolean, FishNbtObject> validatedFish = ValidateItem.isFish(itemStack);
            if(validatedFish.value1()
                    && validatedFish.value2().isOwn()
                    && !InventoryHandler.instance().getTrackedFish().contains(validatedFish.value2().getID())
                    && fishNameToFind.contains(itemStack.getName().getString())
            ) {
                return validatedFish.value2();
            }
        }
        return null;
    }

    public void scanFishListener() {
        Text title = TitleHandler.instance().getTitle();

        if(title.getString().length() != 1 || title.equals(Text.empty())) {
            return;
        }

        if(title.getString().charAt(0) > 0xE000 && title.getString().charAt(0) < 0xE999) {
            this.startScan();
            LoggerHandler._debug("Start finding fish");
        }
    }

    public void scanFishNameListener() {
        Text subTitle = TitleHandler.instance().getSubTitle();

        if(subTitle.equals(Text.empty()) || subTitle.getString().isBlank()) {
            return;
        }

        if(subTitle.getString().charAt(0) > 0xF000 && subTitle.getString().charAt(0) < 0xF999) {
            fishNameToFind = subTitle.getString();
        }
    }

    private void startScan() {
        startScanTime = System.currentTimeMillis();
        scanDone = false;
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "key", Pair.of(Text.literal("value"), Text.empty())
        );
    }
    //endregion
}
