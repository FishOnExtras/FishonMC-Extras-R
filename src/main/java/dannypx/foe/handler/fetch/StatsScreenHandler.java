package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.NotifierHandler;
import dannypx.foe.handler.store.ConstantDataHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.handler.store.StatsDataHandler;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.item.FishNbtObject;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsScreenHandler extends Handler {
    private static StatsScreenHandler INSTANCE = new StatsScreenHandler();

    public static StatsScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new StatsScreenHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private boolean importStats = false;
    private List<Text> statsLore = new ArrayList<>();

    public void setImportStats(boolean importStats) {
        this.importStats = importStats;
    }

    public List<Text> getStatsLore() {
        return statsLore;
    }
    //endregion

    //region Methods
    public void checkStats(GenericContainerScreenHandler genericContainerScreenHandler) {
        if(this.importStats) {
            CodeExecuterHandler.runLater(2, "checkStats", () -> {
                Slot statSlot = genericContainerScreenHandler.getSlot(23);
                Pair<Boolean, Map<String, Map<String, StatsDataHandler.Stat<Integer, Integer>>>> completed = this.extractData(statSlot.getStack());

                if(completed.value1()) {
                    ProfileDataHandler.instance().updateImportStats(true);
                    StatsDataHandler.instance().updateImportStats(true, completed.value2());
                    NotifierHandler.instance().notifyImportStatsCompleted();
                }
            });

            this.importStats = false;
        }
    }

    private Pair<Boolean, Map<String, Map<String, StatsDataHandler.Stat<Integer, Integer>>>> extractData(ItemStack stack) {
        if(stack.get(DataComponentTypes.LORE) != null) {
            List<Text> lines = stack.get(DataComponentTypes.LORE).lines();
            this.statsLore = lines;
            if(lines.size() > 7) {
                Map<String, Map<String, StatsDataHandler.Stat<Integer, Integer>>> newData = StatsDataHandler.instance().getStatsData().fishData;

                int totalFish = this.extractTotal(lines.get(5));
                StatsDataHandler.instance().getStatsData().fishTotal = totalFish;

                // Rarity
                for (int i = 7; i < 12; i++) {
                    Text line = lines.get(i);
                    Triplet<Boolean, String, Integer> data =
                            this.extractStat(ConstantDataHandler.instance().getConstantData().fishData.getOrDefault(FishNbtObject.RARITY, new HashMap<>()), line);

                    if(data.value1()) {
                        Map<String, StatsDataHandler.Stat<Integer, Integer>> newCategoryData = newData
                                .getOrDefault(FishNbtObject.RARITY, new HashMap<>());

                        newCategoryData.put(data.value2(), new StatsDataHandler.Stat<>(data.value3(), totalFish));

                        newData.put(FishNbtObject.RARITY, newCategoryData);
                    }
                }

                // Fish Size
                for (int i = 13; i < 18; i++) {
                    Text line = lines.get(i);
                    Triplet<Boolean, String, Integer> data =
                            this.extractStat(ConstantDataHandler.instance().getConstantData().fishData.getOrDefault(FishNbtObject.FISH_SIZE, new HashMap<>()), line);

                    if(data.value1()) {
                        Map<String, StatsDataHandler.Stat<Integer, Integer>> newCategoryData = newData
                                .getOrDefault(FishNbtObject.FISH_SIZE, new HashMap<>());

                        newCategoryData.put(data.value2(), new StatsDataHandler.Stat<>(data.value3(), totalFish));

                        newData.put(FishNbtObject.FISH_SIZE, newCategoryData);
                    }
                }

                // Variant
                AtomicInteger normalCount = new AtomicInteger(totalFish);
                for (int i = 19; i < 23; i++) {
                    Text line = lines.get(i);
                    Triplet<Boolean, String, Integer> data =
                            this.extractStat(ConstantDataHandler.instance().getConstantData().fishData.getOrDefault(FishNbtObject.VARIANT, new HashMap<>()), line);

                    if(data.value1()) {
                        Map<String, StatsDataHandler.Stat<Integer, Integer>> newCategoryData = newData
                                .getOrDefault(FishNbtObject.VARIANT, new HashMap<>());

                        normalCount.set(normalCount.get() - data.value3());
                        newCategoryData.put(data.value2(), new StatsDataHandler.Stat<>(data.value3(), totalFish));

                        newData.put(FishNbtObject.VARIANT, newCategoryData);
                    }
                }

                newData.getOrDefault(FishNbtObject.VARIANT, new HashMap<>())
                        .put("normal", new StatsDataHandler.Stat<>(normalCount.get(), totalFish));

                return Pair.of(true, newData);
            }
        }
        return Pair.of(false, new HashMap<>());
    }

    private Triplet<Boolean, String, Integer> extractStat(Map<String, Text> constants, Text line) {
        if(line.getSiblings().size() > 2) {
            String field = line.getSiblings().get(1).getString().trim();
            String key = ConstantDataHandler.keysFromField(constants, field).findFirst().orElse(null);
            if(key != null) {
                int amount = TextHelper.toIntFromString(line.getSiblings().get(2).getString());

                return Triplet.of(key, amount);
            }
        }
        return Triplet.ofFalse("", 0);
    }

    private int extractTotal(Text text) {
        return TextHelper.toIntFromString(text.getSiblings().get(2).getString());
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "statsLore", Pair.of(Text.literal("[statsLore]"), TextHelper.literal(getStatsLore()))
        );
    }
    //endregion
}
