package dannypx.foe.item;

import dannypx.foe.helper.ItemStackHelper;
import dannypx.foe.helper.TextHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FishNbtObject extends NbtObject {
    public static final String FISH = "fish";
    public static final String FISH_SIZE = "size";
    public static final String VARIANT = "variant";
    public static final String LENGTH = "length";
    public static final String WEIGHT = "weight";

    public static final int FISH_SIZE_LINE = 7;
    public static final int FISH_SIZE_SIBLING = 2;

    public static final int VARIANT_LINE = 1;
    public static final int VARIANT_SIBLING = 2;

    public FishNbtObject(NbtCompound nbtCompound, ItemStack itemStack) {
        super(nbtCompound, itemStack);
    }

    public String getFish() {
        if(this.contains(FISH)) {
            return this.nbtCompound.getString(FISH);
        }
        return "";
    }

    public float getLength() {
        if(this.contains(LENGTH)) {
            return this.nbtCompound.getFloat(LENGTH);
        }
        return 0f;
    }
    public float getWeight() {
        if(this.contains(WEIGHT)) {
            return this.nbtCompound.getFloat(WEIGHT);
        }
        return 0f;
    }

    public String getVariant() {
        if(this.contains(VARIANT)) {
            return this.nbtCompound.getString(VARIANT);
        }
        return "";
    }

    public Text getVariantText() {
        if(this.itemStack.get(DataComponentTypes.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            try {
                List<Text> textList = this.getLore();
                Text variant = textList.get(VARIANT_LINE).getSiblings().get(VARIANT_SIBLING);
                return TextHelper.trim(variant);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Text.empty();
            }
        }
        return Text.empty();
    }

    public String getFishSize() {
        return this.nbtCompound.getString(FISH_SIZE);
    }

    public Text getFishSizeText() {
        if(this.itemStack.get(DataComponentTypes.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            try {
                List<Text> textList = this.getLore();
                Text fishSize = textList.get(FISH_SIZE_LINE).getSiblings().get(FISH_SIZE_SIBLING);
                return TextHelper.trim(fishSize);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Text.empty();
            }
        }
        return Text.empty();
    }

    public static FishNbtObject of(@NotNull NbtCompound nbtCompound, @NotNull ItemStack itemStack) {
        return new FishNbtObject(nbtCompound, itemStack);
    }

    public static FishNbtObject empty() {
        return new FishNbtObject(ItemStackHelper.getNbt(ItemStack.EMPTY), ItemStack.EMPTY);
    }
}
