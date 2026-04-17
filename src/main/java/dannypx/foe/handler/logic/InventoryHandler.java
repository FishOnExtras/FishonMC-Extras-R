package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.NetworkHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.helper.ItemStackHelper;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.item.*;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import dannypx.foe.type.custom_text.CustomTextValue;
import dannypx.foe.type.custom_text.StringValue;
import dannypx.foe.type.custom_text.TextValue;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class InventoryHandler extends Handler {
    private static InventoryHandler INSTANCE = new InventoryHandler();

    public static InventoryHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new InventoryHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final List<UUID> trackedFish = new ArrayList<>();
    private DefaultedList<ItemStack> snapshotInventory = DefaultedList.ofSize(0);
    private List<Triplet<Long, ItemStack, Integer>> snapshottedItems = new ArrayList<>();
    private List<Triplet<Long, ItemStack, Integer>> snapshottedRemovedItems = new ArrayList<>();
    private FishingRodNbtObject currentFishingRod = FishingRodNbtObject.empty();
    private PetNbtObject currentPet = PetNbtObject.empty();

    private boolean currentlyLoading = false;

    private int currentEmptySlots = 27;

    public List<UUID> getTrackedFish() {
        return trackedFish;
    }

    public DefaultedList<ItemStack> getSnapshotInventory() {
        return snapshotInventory.isEmpty() ? DefaultedList.ofSize(0) : snapshotInventory;
    }

    public List<Triplet<Long, ItemStack, Integer>> getSnapshottedItems() {
        return snapshottedItems;
    }

    public List<ItemStack> getSnapshottedItemstacks() {
        return this.snapshottedItems.stream().map(Triplet::value2).toList();
    }

    protected void setCurrentFishingRod(FishingRodNbtObject currentFishingRod) {
        this.currentFishingRod = currentFishingRod;
    }

    public FishingRodNbtObject getCurrentFishingRod() {
        return this.currentFishingRod;
    }

    protected  void setCurrentPet(PetNbtObject currentPet) {
        this.currentPet = currentPet;
    }

    public PetNbtObject getCurrentPet() {
        return this.currentPet;
    }

    public boolean hasPet() {
        return this.currentPet.getItemStack() != ItemStack.EMPTY;
    }

    public int getCurrentEmptySlots() {
        return currentEmptySlots;
    }

    public Pair<Boolean, CustomTextValue> getInventory(String[] params) {
        if(params.length > 0) {
            Pattern fieldPattern = Pattern.compile("^(fishing_rod|pet|armor|empty_slots|held_item|slot)$");

            if(fieldPattern.matcher(params[0]).matches()) {
                return switch(params[0]) {
                    case "fishing_rod" -> {
                        if(params.length >= 2) {
                            yield switch(params[1]) {
                                case "name" -> PlaceholderHandler.getTextValue(new TextValue(getCurrentFishingRod().getName()));
                                case "line" -> {
                                    List<NbtObject> list = getCurrentFishingRod().getLineItem();
                                    if(!list.isEmpty()) {
                                        yield switch(params[2]) {
                                            case "name" -> PlaceholderHandler.getTextValue(new TextValue(list.getFirst().getName()));
                                            default -> PlaceholderHandler.getNbtTextValue(list.getFirst(), params[2]);
                                        };
                                    }
                                    yield PlaceholderHandler.noResult();
                                }
                                case "reel" -> {
                                    List<NbtObject> list = getCurrentFishingRod().getReelItem();
                                    if(!list.isEmpty()) {
                                        yield switch(params[2]) {
                                            case "name" -> PlaceholderHandler.getTextValue(new TextValue(list.getFirst().getName()));
                                            default -> PlaceholderHandler.getNbtTextValue(list.getFirst(), params[2]);
                                        };
                                    }
                                    yield PlaceholderHandler.noResult();
                                }
                                case "pole" -> {
                                    List<NbtObject> list = getCurrentFishingRod().getPoleItem();
                                    if(!list.isEmpty()) {
                                        yield switch(params[2]) {
                                            case "name" -> PlaceholderHandler.getTextValue(new TextValue(list.getFirst().getName()));
                                            default -> PlaceholderHandler.getNbtTextValue(list.getFirst(), params[2]);
                                        };
                                    }
                                    yield PlaceholderHandler.noResult();
                                }
                                default -> PlaceholderHandler.getNbtTextValue(getCurrentFishingRod(), params[1]);
                            };
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "pet" -> {
                        if(params.length == 2
                                && hasPet()
                        ) {
                            yield switch(params[1]) {
                                case "name" -> PlaceholderHandler.getTextValue(new TextValue(getCurrentPet().getName()));
                                case "level" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(getCurrentPet().getLevel())));
                                case "level_progress" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getProgress() * 100, 2)));
                                case "rating" -> PlaceholderHandler.getTextValue(new TextValue(getCurrentPet().getRatingText()));
                                case "rating_percent" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getTotalPercent() * 100, 2)));
                                case "rarity" -> PlaceholderHandler.getTextValue(new TextValue(getCurrentPet().getRarityText()));
                                case "location_luck_percent" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getLocationPercentMaxLuck() * 100, 2)));
                                case "location_scale_percent" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getLocationPercentMaxScale() * 100, 2)));
                                case "climate_luck_percent" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getClimatePercentMaxLuck() * 100, 2)));
                                case "climate_scale_percent" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getClimatePercentMaxScale() * 100, 2)));
                                case "location_luck" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getLocationMaxLuck(), 0)));
                                case "location_scale" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getLocationMaxScale(), 0)));
                                case "climate_luck" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getClimateMaxLuck(), 0)));
                                case "climate_scale" -> PlaceholderHandler.getTextValue(new StringValue(TextHelper.floatToString(getCurrentPet().getClimateMaxScale(), 0)));
                                default -> PlaceholderHandler.getNbtTextValue(getCurrentPet(), params[1]);
                            };
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "armor" -> {
                        if(params.length == 3
                                && minecraftClient.player != null
                        ) {
                            ItemStack stack = ItemStack.EMPTY;

                            switch(params[1]) {
                                case "chestplate" -> stack = minecraftClient.player.getInventory().armor.get(EquipmentSlot.CHEST.getEntitySlotId());
                                case "leggings" -> stack = minecraftClient.player.getInventory().armor.get(EquipmentSlot.LEGS.getEntitySlotId());
                                case "boots" -> stack = minecraftClient.player.getInventory().armor.get(EquipmentSlot.FEET.getEntitySlotId());
                            }

                            Pair<Boolean, NbtObject> validatedItem = ValidateItem.isServerItem(stack);

                            if(validatedItem.value1()) {
                                yield switch(params[2]) {
                                    case "name" -> PlaceholderHandler.getTextValue(new TextValue(validatedItem.value2().getName()));
                                    default -> PlaceholderHandler.getNbtTextValue(validatedItem.value2(), params[2]);
                                };
                            }
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "empty_slots" -> PlaceholderHandler.getTextValue(new StringValue(String.valueOf(getCurrentEmptySlots())));
                    case "held_item" -> {
                        if(params.length >= 2
                                && minecraftClient.player != null
                        ) {
                            ItemStack heldItem = minecraftClient.player.getInventory().getMainHandStack();
                            if(!heldItem.isEmpty()) {
                                yield switch (params[1]) {
                                    case "name" -> PlaceholderHandler.getTextValue(new TextValue(heldItem.getName()));
                                    case "tooltip" -> {
                                        if(params.length >= 3) {
                                            if(heldItem.get(DataComponentTypes.LORE) != null) {
                                                List<Text> lines = heldItem.get(DataComponentTypes.LORE).lines();
                                                try {
                                                    int index = Integer.parseInt(params[2]);

                                                    if(index < lines.size()) {
                                                        yield PlaceholderHandler.getTextValue(new TextValue(lines.get(index)));
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                        yield PlaceholderHandler.noResult();
                                    }
                                    default -> PlaceholderHandler.getNbtTextValue(heldItem, params[2]);
                                };
                            }
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "slot" -> {
                        if(params.length >= 3
                                && minecraftClient.player != null
                        ) {
                            try {
                                int slot = Integer.parseInt(params[1]);
                                ItemStack stack = minecraftClient.player.getInventory().getStack(slot);

                                if(!stack.isEmpty()) {
                                    yield switch (params[2]) {
                                        case "name" -> PlaceholderHandler.getTextValue(new TextValue(stack.getName()));
                                        case "tooltip" -> {
                                            if(params.length >= 4) {
                                                if(stack.get(DataComponentTypes.LORE) != null) {
                                                    List<Text> lines = stack.get(DataComponentTypes.LORE).lines();
                                                    try {
                                                        int index = Integer.parseInt(params[3]);

                                                        if(index < lines.size()) {
                                                            yield PlaceholderHandler.getTextValue(new TextValue(lines.get(index)));
                                                        }
                                                    } catch (Exception ignored) {}
                                                }
                                            }
                                            yield PlaceholderHandler.noResult();
                                        }
                                        default -> PlaceholderHandler.getNbtTextValue(stack, params[2]);
                                    };
                                }
                            } catch (Exception ignored) {}
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    default -> PlaceholderHandler.noResult();
                };
            }
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void tick() {
        if(minecraftClient.player != null) {
            this.tickInventory();
            this.snapshotFishingRod();
            this.snapshotPet();
            this.snapshotEmptySlots();
            this.checkSnapshottedItems();
        }
    }

    private void tickInventory() {
        if(!snapshotInventory.isEmpty()) {
            DefaultedList<ItemStack> oldInventory = snapshotInventory;
            DefaultedList<ItemStack> newInventory = minecraftClient.player.getInventory().main;

            for(int i = 0; i < newInventory.size(); i++) {
                ItemStack oldStack = oldInventory.get(i);
                ItemStack newStack = newInventory.get(i);

                // New item in slot
                if ((oldStack.isEmpty() && !newStack.isEmpty())
                        || (newStack.isEmpty() && !oldStack.isEmpty())
                ) {
                    this.snapshotInventory();
                    int finalI = i;
                    if(!newStack.isEmpty() &&
                            snapshottedRemovedItems.stream()
                                    .noneMatch(
                                            removedItem -> removedItem.value3() == finalI
                                            && ItemStack.areItemsAndComponentsEqual(removedItem.value2(), newStack)
                                    )
                    ) this.addToSnapshotItems(newStack, newStack.getCount());
                    if(newStack.isEmpty()) this.addToRemovedSnapshotItems(oldStack, i);
                }

                // Same item, stack size changed
                if (!newStack.isEmpty()
                        && !oldStack.isEmpty()
                        && oldStack.getCount() != newStack.getCount()) {
                    this.snapshotInventory();
                    this.addToSnapshotItems(newStack, newStack.getCount() - oldStack.getCount());
                }
            }
        } else {
            if(!currentlyLoading) {
                currentlyLoading = true;
                CodeExecuterHandler.runLater(100, "tickInventory > snapshotInventory", this::snapshotInventory);
            }
        }
    }

    public void onLeave() {
        this.reset();
    }

    public void reset() {
        this.currentlyLoading = false;
        this.snapshotInventory.clear();
        this.snapshottedItems.clear();
    }

    private void checkSnapshottedItems() {
        snapshottedItems.removeIf(item -> item.value1() > System.currentTimeMillis() + 1000L);
        snapshottedRemovedItems.removeIf(item -> item.value1() > System.currentTimeMillis() + 60L + NetworkHandler.instance().getPing());
    }

    private void addToSnapshotItems(ItemStack newStack, int count) {
        LoggerHandler._debug("Snapshotted Item: " + newStack.getName().getString() + " at " + System.currentTimeMillis());
        snapshottedItems.add(Triplet.of(System.currentTimeMillis(), newStack, count));
    }

    private void addToRemovedSnapshotItems(ItemStack oldStack, int slot) {
        snapshottedRemovedItems.add(Triplet.of(System.currentTimeMillis(), oldStack, slot));
    }

    private void snapshotEmptySlots() {
        int empty = 0;

        for (ItemStack stack : minecraftClient.player.getInventory().main) {
            if (stack.isEmpty()) {
                empty++;
            }
        }

        if(currentEmptySlots != empty) {
            currentEmptySlots = empty;
            NotifierHandler.instance().notifyEmptySlots(currentEmptySlots);
        }
    }

    private void snapshotPet() {
        if(ProfileDataHandler.instance().getProfileData().activePetSlot != -1) {
            ItemStack pet = minecraftClient.player.getInventory().main.get(ProfileDataHandler.instance().getProfileData().activePetSlot);
            if(!pet.isEmpty() && !ItemStack.areItemsAndComponentsEqual(currentPet.getItemStack(), pet)) {
                Pair<Boolean, @Nullable PetNbtObject> validatedPet = ValidateItem.isPet(pet);
                if(validatedPet.value1()) {
                    this.setCurrentPet(validatedPet.value2());
                }
            }
        } else if(ProfileDataHandler.instance().getProfileData().activePetSlot == -1
                && currentPet.getItemStack() != ItemStack.EMPTY
        ) {
            currentPet = PetNbtObject.empty();
        }
    }

    private void snapshotFishingRod() {
        ItemStack fishingRod = minecraftClient.player.getInventory().main.getFirst();
        if(!fishingRod.isEmpty() && !ItemStack.areItemsAndComponentsEqual(currentFishingRod.getItemStack(), fishingRod)) {
            Pair<Boolean, @Nullable FishingRodNbtObject> validatedFishingRod = ValidateItem.isFishingRod(fishingRod);
            if(validatedFishingRod.value1()) {
                this.setCurrentFishingRod(validatedFishingRod.value2());
            }
        }
    }

    public void trackFishOffSide() {
        if(minecraftClient.player != null
                && CatchingHandler.instance().isScanDone()
        ) {
            this.trackAllFish();
        }
    }

    public void snapshotInventory() {
        if(minecraftClient.player != null) {
            snapshotInventory = ItemStackHelper.deepCopy(
                    minecraftClient.player.getInventory().main,
                    ItemStack.EMPTY,
                    stack -> stack.isEmpty() ? ItemStack.EMPTY : stack.copy()
            );
        }
    }

    public void addToTrackedFish(UUID uuid) {
        if (!trackedFish.contains(uuid)) {
            trackedFish.add(uuid);
        }
    }

    public boolean trackAllFish() {
        if(minecraftClient.player != null) {
            trackedFish.clear();
            minecraftClient.player.getInventory().main.forEach(itemStack -> {
                Pair<Boolean, FishNbtObject> validatedItem = ValidateItem.isFish(itemStack);
                if(validatedItem.value1() && validatedItem.value2().isOwn()) {
                    this.addToTrackedFish(validatedItem.value2().getID());
                }
            });
            LoggerHandler._debug("Tracked Fish: " + trackedFish.size());
            return true;
        }
        return false;
    }

    public NbtObject getCurrentHeldItem() {
        if(minecraftClient.player != null) {
            ItemStack heldItem = minecraftClient.player.getInventory().getMainHandStack();
            Pair<Boolean, NbtObject> validatedItem = ValidateItem.isServerItem(heldItem);
            if(validatedItem.value1()) {
                return validatedItem.value2();
            }
        }
        return NbtObject.empty();
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "trackedFish", Pair.of(Text.literal("[trackedFish]"), TextHelper.literal(getTrackedFish())),
                "snapshotInventory", Pair.of(Text.literal("[snapshotInventory]"), TextHelper.literal(
                        ItemStackHelper.itemStackListToJson(getSnapshotInventory())
                )),
                "currentFishingRod", Pair.of(Text.literal("[currentFishingRod]"), TextHelper.literal(getCurrentFishingRod().getItemStack())),
                "currentPet", Pair.of(Text.literal("[currentPet]"), TextHelper.literal(getCurrentPet().getItemStack())),
                "currentHeldItem", Pair.of(Text.literal("[currentHeldItem]"), TextHelper.literal(getCurrentHeldItem())),
                "snapshottedItems", Pair.of(Text.literal("[snapshottedItems]"), TextHelper.literal(getSnapshottedItemstacks()))
        );
    }
    //endregion
}
