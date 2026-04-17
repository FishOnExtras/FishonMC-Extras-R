package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.item.ArmorNbtObject;
import dannypx.foe.item.ValidateItem;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArmorRollScreenHandler extends Handler {
    private static ArmorRollScreenHandler INSTANCE = new ArmorRollScreenHandler();

    public static ArmorRollScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ArmorRollScreenHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private List<ItemStack> rollList = new ArrayList<>();
    private ArmorNbtObject armor = ArmorNbtObject.empty();

    public List<ItemStack> getRollList() {
        return rollList;
    }

    public ArmorNbtObject getArmor() {
        return armor;
    }
    //endregion

    //region Methods
    public void checkArmorRolls(GenericContainerScreenHandler screenHandler) {
        CodeExecuterHandler.runLater(2, "checkArmorRolls", () -> {
            rollList.clear();
            armor = ArmorNbtObject.empty();
            for (int i = 11; i < 16; i++) {
                rollList.add(screenHandler.getSlot(i).getStack());
            }

            Pair<Boolean, ArmorNbtObject> validatedArmor = ValidateItem.isArmor(screenHandler.getSlot(31).getStack());
            if(validatedArmor.value1()) {
                armor = validatedArmor.value2();
            }
        });
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    @Override
    protected Map<String, Pair<MutableText, MutableText>> _getFields() {
        return Map.of(
                "key", Pair.of(Text.literal("value"), Text.empty())
        );
    }
    //endregion
}
