package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.NotifierHandler;
import dannypx.foe.handler.store.QuestDataHandler;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuestScreenHandler extends Handler {
    private static QuestScreenHandler INSTANCE = new QuestScreenHandler();

    public static QuestScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new QuestScreenHandler();
        }
        return INSTANCE;
    }

    //region Fields
    //endregion

    //region Methods
    public void checkQuests(GenericContainerScreenHandler genericContainerScreenHandler) {
        List<QuestDataHandler.Quest> questList = new ArrayList<>();

        CodeExecuterHandler.runLater(2, "checkQuests", () -> {
            genericContainerScreenHandler.slots.forEach(slot -> {
                if (minecraftClient.player != null
                        && slot.inventory != minecraftClient.player.getInventory()
                        && slot.getStack().isIn(ItemTags.SHULKER_BOXES)
                        && slot.getStack().getItem() != Items.WHITE_SHULKER_BOX
                        && slot.getStack().getName().getString().startsWith("Fishing Quest")
                ) {
                    QuestDataHandler.Quest quest = this.extractQuestData(slot.getStack());

                    if(quest != null) {
                        questList.add(quest);
                    }
                }
            });

            if(!questList.isEmpty()) {
                QuestDataHandler.instance().setQuest(questList);
            }
        });
    }

    private QuestDataHandler.Quest extractQuestData(ItemStack stack) {
        if(stack.get(DataComponentTypes.LORE) != null) {
            List<Text> lines = stack.get(DataComponentTypes.LORE).lines();
            if(lines.size() > 6) {
                String goal = lines.get(3).getSiblings().get(3).getString().toLowerCase(Locale.US).trim();
                int max = Integer.parseInt(lines.get(6).getSiblings().get(5).getString());
                int current = Integer.parseInt(lines.get(6).getSiblings().get(3).getString());

                String location = lines.get(4).getString();

                if(location.contains(BossBarHandler.instance().getLocation().getString().trim())) {
                    return new QuestDataHandler.Quest(goal, max, current);
                }
            }
        }
        return null;
    }

    public void checkForCompletedQuests() {
        List<QuestDataHandler.Quest> quests = QuestDataHandler.instance().getQuestData().questList.getOrDefault(BossBarHandler.instance().getLocation().getString(), new ArrayList<>());

        quests.forEach(quest -> {
            if(quest.isDone()) {
                NotifierHandler.instance().notifyQuest(quest);
            }
        });
    }

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
        );
    }
    //endregion
}
