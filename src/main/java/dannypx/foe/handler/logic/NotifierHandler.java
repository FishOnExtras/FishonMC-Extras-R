package dannypx.foe.handler.logic;

import com.mojang.brigadier.StringReader;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.store.*;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.item.FishNbtObject;
import dannypx.foe.item.NbtObject;
import dannypx.foe.item.PetNbtObject;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class NotifierHandler extends Handler {
    private static NotifierHandler INSTANCE = new NotifierHandler();

    public static NotifierHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new NotifierHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final List<Notification> notifications = new ArrayList<>();
    private final List<UUID> removeQueue = new ArrayList<>();
    private final Map<String, UUID> persistentNotifications = new HashMap<>();

    public static final String IMPORT_STATS_KEY = "importStatsKey";
    public static final String IMPORT_CREW_KEY = "importCrewKey";
    public static final String EMPTY_SLOTS_KEY = "emptySlotsKey";

    private final int WIDTH = 200;
    private final int BOX_PADDING = 7;
    private final int CONTENT_WIDTH = WIDTH - BOX_PADDING * 2 - 2;
    private final int ICON_CONTENT_WIDTH = CONTENT_WIDTH - 18;

    public List<Notification> getNotifications() {
        return notifications;
    }
    //endregion

    //region Methods
    public void init() {
        if(!ProfileDataHandler.instance().getProfileData().hasImportedStats) {
            this.notifyImportStats();
        }
    }

    public void tick() {
        boolean isRemoved = notifications.removeIf(notification -> removeQueue.contains(notification.uuid));
        if(isRemoved) removeQueue.clear();

        notifications.forEach(notification -> {
            if(System.currentTimeMillis() > notification.startTime + notification.notificationTime * 1000L) {
                this.removeNotification(notification.uuid);
            }
        });
    }

    public UUID addNotification(Notification notification) {
        notifications.add(notification);
        return notification.uuid;
    }

    public void removeNotification(UUID uuid) {
        removeQueue.add(uuid);
    }

    public void removeNotification(String key) {
        if(persistentNotifications.containsKey(key)) {
            UUID uuid = persistentNotifications.remove(key);
            this.removeNotification(uuid);
        }
    }

    public void notifyFish(
            FishNbtObject fish,
            Pair<String, Integer> rarityDrystreak,
            Pair<String, Integer> variantDrystreak,
            Pair<String, Integer> sizeDryStreak
    ) {
        if(!Configs.hudConfig.showFishCatchNotification.get()) {
            return;
        }

        Text tagText = !Objects.equals(fish.getVariant(), "normal")
                ? TextHelper.concat(fish.getVariantText(), Text.literal(" "), fish.getRarityText())
                : TextHelper.concat(fish.getRarityText());
        Text rarityText = fish.getRarityText();
        Text variantText = fish.getVariantText();
        Text sizeText = fish.getFishSizeText();

        Text lengthText = TextHelper.concat(
                Text.literal(TextHelper.floatToString(fish.getLength(), 2)).formatted(Formatting.GRAY),
                Text.literal("in ").formatted(Formatting.GRAY)
        );

        Text weightText = TextHelper.concat(
                Text.literal(TextHelper.floatToString(fish.getWeight(), 2)).formatted(Formatting.GRAY),
                Text.literal("lb ").formatted(Formatting.GRAY)
        );

        List<Text> notifTextList = new ArrayList<>(Arrays.asList(
                tagText,
                fish.getName(),
                TextHelper.concat(fish.getFishSizeText(), lengthText, weightText),
                Text.empty(),
                Text.literal(" - Drystreaks before catch").formatted(Formatting.GRAY),
                TextHelper.concat(rarityText, TextHelper.literal(rarityDrystreak.value2())),
                TextHelper.concat(sizeText, TextHelper.literal(sizeDryStreak.value2()))
        ));

        if(!Objects.equals(fish.getVariant(), "normal")) {
            notifTextList.add(TextHelper.concat(variantText, Text.literal(" "), TextHelper.literal(variantDrystreak.value2())));
        }

        int rows = !Configs.hudConfig.showFishDrystreakNotification.get() ? 3 : notifTextList.size();

        this.addNotification(
                new NotifierHandler.Notification(fish.getItemStack(),
                        rows, 1,
                        Configs.hudConfig.fishDismissalTime.get(),
                        notifTextList
                )
        );
    }

    public void notifyPet(PetNbtObject pet, Pair<String, Integer> rarityDrystreak, Pair<String, Integer> ratingDrystreak) {
        if(!Configs.hudConfig.showPetCatchNotification.get()) {
            return;
        }

        Text petText = TextHelper.concat(pet.getRarityText(), Text.literal(" ") , pet.getName());

        List<Text> notifTextList =  new ArrayList<>(Arrays.asList(
                petText,
                pet.getRatingText(),
                Text.empty(),
                Text.literal(" - Drystreaks before catch").formatted(Formatting.GRAY),
                TextHelper.concat(pet.getRarityText(), Text.literal(" "), TextHelper.literal(rarityDrystreak.value2())),
                TextHelper.concat(pet.getRatingText(), Text.literal(" "), TextHelper.literal(ratingDrystreak.value2()))
        ));

        int rows = !Configs.hudConfig.showPetsDrystreakNotification.get() ? 2 : notifTextList.size();

        this.addNotification(
                new Notification(pet.getItemStack(),
                        rows, 1,
                        Configs.hudConfig.petDismissalTime.get(),
                        notifTextList
                )
        );
    }

    public void notifyItem(NbtObject item, int count, Pair<String, Integer> itemDrystreak) {
        if(!Configs.hudConfig.showOtherItemCatchNotification.get()) {
            return;
        }

        Text itemText = TextHelper.concat(item.getName(), Text.literal(" "), TextHelper.literal(count), Text.literal("x").formatted(Formatting.GRAY));
        Text typeText = Text.literal(TextHelper.convertField(itemDrystreak.value1()));


        List<Text> notifTextList =  new ArrayList<>(Arrays.asList(
                itemText,
                Text.empty(),
                Text.literal(" - Drystreak before catch").formatted(Formatting.GRAY),
                TextHelper.concat(typeText, Text.literal(" ") ,TextHelper.literal(itemDrystreak.value2()))
        ));

        int rows = !Configs.hudConfig.showOtherItemDrystreakNotification.get() ? 1 : notifTextList.size();

        this.addNotification(
                new Notification(item.getItemStack(),
                        rows, 1,
                        Configs.hudConfig.otherDismissalTime.get(),
                        notifTextList
                )
        );
    }

    public void notifyQuest(QuestDataHandler.Quest quest) {
        if(!Configs.hudConfig.showQuestCompletionNotification.get()) {
            return;
        }

        Text completionText = Text.literal("Completed quest").formatted(Formatting.GREEN);
        Text goalText = TextHelper.concat(
                ConstantDataHandler.instance().getConstantFishText(quest.goal),
                Text.literal(" "),
                TextHelper.literal(quest.current).formatted(Formatting.YELLOW),
                Text.literal("/").formatted(Formatting.GRAY),
                TextHelper.literal(quest.max).formatted(Formatting.WHITE)
        );

        this.addNotification(
                new Notification(
                        2, 1,
                        Configs.hudConfig.questDismissalTime.get(),
                        new ArrayList<>(Arrays.asList(completionText, goalText))
                )
        );
    }

    public void notifyImportStats() {
        UUID importStatsUUID = this.addNotification(
                new Notification(11, 1,
                        new ArrayList<>(Arrays.asList(
                                Text.literal("You have yet to import your stats").formatted(Formatting.GOLD),
                                Text.empty(),
                                TextHelper.concat(
                                        Text.literal("Do "),
                                        Text.literal("/foe stats import ").formatted(Formatting.GREEN),
                                        Text.literal("to import")
                                ),
                                Text.literal("your stats"),
                                Text.literal("This will open the stats screen").formatted(Formatting.GRAY, Formatting.ITALIC),
                                Text.literal("and import your stats").formatted(Formatting.GRAY, Formatting.ITALIC),
                                Text.empty(),
                                TextHelper.concat(
                                        Text.literal("Do "),
                                        Text.literal("/foe stats cancel ").formatted(Formatting.GREEN),
                                        Text.literal("to cancel")
                                ),
                                Text.literal("this notification"),
                                TextHelper.concat(
                                        Text.literal("You can still do ").formatted(Formatting.GRAY, Formatting.ITALIC),
                                        Text.literal("/foe stats ").formatted(Formatting.GREEN, Formatting.ITALIC)
                                ),
                                TextHelper.concat(
                                        Text.literal("import ").formatted(Formatting.GREEN, Formatting.ITALIC),
                                        Text.literal("to import at a later time").formatted(Formatting.GRAY, Formatting.ITALIC)
                                )
                        ))
                )
        );

        this.persistentNotifications.put(IMPORT_STATS_KEY, importStatsUUID);
    }

    public void notifyImportCrew() {
        UUID importCrewUUID = this.addNotification(
                new Notification(12, 1,
                        new ArrayList<>(Arrays.asList(
                                Text.literal("You have yet to import ").formatted(Formatting.GOLD),
                                Text.literal("your crew info").formatted(Formatting.GOLD),
                                Text.empty(),
                                TextHelper.concat(
                                        Text.literal("Do "),
                                        Text.literal("/foe crew import ").formatted(Formatting.GREEN),
                                        Text.literal("to import")
                                ),
                                Text.literal("crew info"),
                                Text.literal("This will open the crew screen").formatted(Formatting.GRAY, Formatting.ITALIC),
                                Text.literal("and import your crew info").formatted(Formatting.GRAY, Formatting.ITALIC),
                                Text.empty(),
                                TextHelper.concat(
                                        Text.literal("Do "),
                                        Text.literal("/foe crew cancel ").formatted(Formatting.GREEN),
                                        Text.literal("to cancel")
                                ),
                                Text.literal("this notification"),
                                TextHelper.concat(
                                        Text.literal("You can still do ").formatted(Formatting.GRAY, Formatting.ITALIC),
                                        Text.literal("/foe crew ").formatted(Formatting.GREEN, Formatting.ITALIC)
                                ),
                                TextHelper.concat(
                                        Text.literal("import ").formatted(Formatting.GREEN, Formatting.ITALIC),
                                        Text.literal("to import at a later time").formatted(Formatting.GRAY, Formatting.ITALIC)
                                )
                        ))
                )
        );

        this.persistentNotifications.put(IMPORT_CREW_KEY, importCrewUUID);
    }

    public void notifyImportStatsCompleted() {
        this.addNotification(
                new Notification(1, 1,
                        10,
                        Collections.singletonList(
                                Text.literal("✔ Stats imported successfully").formatted(Formatting.GREEN)
                        )
                )
        );
    }

    public void notifyImportCrewCompleted() {
        this.addNotification(
                new Notification(1, 1,
                        10,
                        Collections.singletonList(
                                Text.literal("✔ Crew imported successfully").formatted(Formatting.GREEN)
                        )
                )
        );
    }

    public void notifyEmptySlots(int currentEmptySlots) {
        this.removeNotification(EMPTY_SLOTS_KEY);

        if(!Configs.hudConfig.showEmptySlotsNotification.get()) {
            return;
        }

        if(currentEmptySlots <= Configs.hudConfig.showNotificationAtEmptySlots.get()) {
            Text notifText = currentEmptySlots == 0
                    ? Text.literal("You have a full inventory!").formatted(Formatting.DARK_RED)
                    : Text.literal("You nearly have a full inventory").formatted(Formatting.RED);

            UUID emptySlotsUUID = this.addNotification(new Notification(
                2, 1,
                    new ArrayList<>(Arrays.asList(
                            notifText,
                            TextHelper.concat(
                                    Text.literal("Slots left: ").formatted(Formatting.GRAY),
                                    TextHelper.literal(currentEmptySlots)
                            )
                    ))
            ));

            this.persistentNotifications.put(EMPTY_SLOTS_KEY, emptySlotsUUID);
        }
    }

    public void notifyPlayerStatus(boolean isJoin, Pair<String, ItemStack> player) {
        if(!Configs.hudConfig.showCrewStatusNotification.get()) {
            return;
        }

        Text icon = isJoin
                ? Text.literal("+ ").formatted(Formatting.BOLD, Formatting.DARK_GREEN)
                : Text.literal("- ").formatted(Formatting.BOLD, Formatting.DARK_RED);

        Text status = isJoin
                ? Text.literal(" has joined").formatted(Formatting.GREEN)
                : Text.literal(" has left").formatted(Formatting.RED);

        this.addNotification(
                new Notification(
                        player.value2(), 1, 1, Configs.hudConfig.crewDismissalTime.get(),
                        List.of(TextHelper.concat(
                                icon,
                                Text.literal(player.value1()).formatted(Formatting.YELLOW),
                                status
                        ))
                )
        );
    }

    public void notifyNotifier(String notificationId) {
        CustomNotificationDataHandler.CustomNotification notification = CustomNotificationDataHandler.instance().getCustomNotificationData().notificationList.getOrDefault(notificationId, null);

        if(notification != null && minecraftClient.player != null) {
            ItemStack itemStack = ItemStack.EMPTY;

            if(!notification.icon.isBlank()) {
                RegistryWrapper.WrapperLookup lookup = minecraftClient.player.getRegistryManager();

                ItemStringReader reader = new ItemStringReader(lookup);
                StringReader stringReader = new StringReader(notification.icon);
                try {
                    ItemStringReader.ItemResult result = reader.consume(stringReader);

                    itemStack = new ItemStack(result.item(), 1);
                    itemStack.applyUnvalidatedChanges(result.components());
                } catch (Exception e) {
                    LoggerHandler.error(e);
                }
            }

            List<MutableText> lines = notification.textLines.stream().map(text -> text.replace("&", "§")).map(PlaceholderHandler::parsePlaceholderFromString).filter(Pair::value1).map(Pair::value2).toList();
            List<Text> newLines = new ArrayList<>();

            lines.forEach(line -> newLines.addAll(TextHelper.wrapStyledText(line, notification.icon.isBlank() ? CONTENT_WIDTH : ICON_CONTENT_WIDTH, true, minecraftClient.textRenderer)));

            if(itemStack == ItemStack.EMPTY) {
                this.addNotification(
                        new Notification(
                            newLines.size(), 1, 10,
                            newLines
                        )
                );
            } else {
                this.addNotification(
                        new Notification(
                                itemStack,
                                newLines.size(), 1, 10,
                                newLines
                        )
                );
            }


        }
    }
    //endregion

    //region Notification Object
    public static class Notification {
        public final ItemStack item;
        public final int rows;
        public final int columns;
        public final List<Text> textList;
        protected final long startTime;
        protected final int notificationTime;
        protected final UUID uuid;

        public Notification(
                ItemStack item,
                int rows,
                int columns,
                int notificationTime,
                List<Text> texts
        ) {
            this.item = item;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = notificationTime;
            this.textList = texts;
            this.uuid = UUID.randomUUID();
        }

        public Notification(
                int rows,
                int columns,
                int notificationTime,
                List<Text> texts
        ) {
            this.item = ItemStack.EMPTY;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = notificationTime;
            this.textList = texts;
            this.uuid = UUID.randomUUID();
        }

        public Notification(
                ItemStack item,
                int rows,
                int columns,
                List<Text> texts
        ) {
            this.item = item;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = Integer.MAX_VALUE;
            this.textList = texts;
            this.uuid = UUID.randomUUID();
        }

        public Notification(
                int rows,
                int columns,
                List<Text> texts
        ) {
            this.item = ItemStack.EMPTY;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = Integer.MAX_VALUE;
            this.textList = texts;
            this.uuid = UUID.randomUUID();
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "notifications", Pair.of(Text.literal("[notifications]]"), TextHelper.literal(getNotifications()))
        );
    }
    //endregion
}
