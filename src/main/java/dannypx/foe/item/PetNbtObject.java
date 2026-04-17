package dannypx.foe.item;

import dannypx.foe.helper.ItemStackHelper;
import dannypx.foe.helper.TextHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PetNbtObject extends NbtObject {

    public static final String LEVEL = "level";
    public static final String XP_NEED = "xp_need";
    public static final String XP_CURRENT = "xp_cur";
    public static final String RATING = "rating";
    public static final String LOCATION_BASE = "lbase";
    public static final String CLIMATE_BASE = "cbase";
    public static final String PERCENT_MAX_BASE = "percent_max";
    public static final String MAX_BASE = "cur_max";
    public static final String ITEM = "item";
    public static final String SKIN = "skin";

    public static final int RATING_LINE = 15;
    public static final int RATING_SIBLING = 2;

    public static final int C_BASE_LUCK_LINE = 8;
    public static final int C_BASE_SCALE_LINE = 9;
    public static final int L_BASE_LUCK_LINE = 12;
    public static final int L_BASE_SCALE_LINE = 13;

    public PetNbtObject(NbtCompound nbtCompound, ItemStack itemStack) {
        super(nbtCompound, itemStack);
    }

    public int getLevel() {
        if(this.contains(LEVEL)) {
            return this.nbtCompound.getInt(LEVEL);
        }
        return 0;
    }

    public float getProgress() {
        if(this.contains(XP_NEED) && this.contains(XP_CURRENT)) {
            float neededXP = this.nbtCompound.getFloat(XP_NEED);
            float currentXP = this.nbtCompound.getFloat(XP_CURRENT);
            return Math.min(currentXP / neededXP, 1f);
        }
        return 0f;
    }

    public Text getRatingText() {
        if(this.itemStack.get(DataComponentTypes.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            try {
                List<Text> textList = this.getLore();
                Text rating = textList.get(RATING_LINE).getSiblings().get(RATING_SIBLING);
                return TextHelper.trim(rating);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Text.empty();
            }
        }
        return Text.empty();
    }

    public NbtList getLocationBase() {
        if(this.contains(LOCATION_BASE)) {
            return this.nbtCompound.getList(LOCATION_BASE, NbtElement.COMPOUND_TYPE);
        }
        return new NbtList();
    }

    public NbtList getClimateBase() {
        if(this.contains(CLIMATE_BASE)) {
            return this.nbtCompound.getList(CLIMATE_BASE, NbtElement.COMPOUND_TYPE);
        }
        return new NbtList();
    }

    public float getLocationPercentMaxLuck() {
        NbtList base = this.getLocationBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(0);
            if(compound.contains(PERCENT_MAX_BASE)) {
                return compound.getFloat(PERCENT_MAX_BASE);
            }
        }
        return 0f;
    }

    public float getLocationPercentMaxScale() {
        NbtList base = this.getLocationBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(1);
            if(compound.contains(PERCENT_MAX_BASE)) {
                return compound.getFloat(PERCENT_MAX_BASE);
            }
        }
        return 0f;
    }

    public float getClimatePercentMaxLuck() {
        NbtList base = this.getClimateBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(0);
            if(compound.contains(PERCENT_MAX_BASE)) {
                return compound.getFloat(PERCENT_MAX_BASE);
            }
        }
        return 0f;
    }

    public float getClimatePercentMaxScale() {
        NbtList base = this.getClimateBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(1);
            if(compound.contains(PERCENT_MAX_BASE)) {
                return compound.getFloat(PERCENT_MAX_BASE);
            }
        }
        return 0f;
    }

    public float getLocationMaxLuck() {
        NbtList base = this.getLocationBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(0);
            if(compound.contains(MAX_BASE)) {
                return compound.getInt(MAX_BASE);
            }
        }
        return 0f;
    }

    public float getLocationMaxScale() {
        NbtList base = this.getLocationBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(1);
            if(compound.contains(MAX_BASE)) {
                return compound.getInt(MAX_BASE);
            }
        }
        return 0f;
    }

    public float getClimateMaxLuck() {
        NbtList base = this.getClimateBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(0);
            if(compound.contains(MAX_BASE)) {
                return compound.getInt(MAX_BASE);
            }
        }
        return 0f;
    }

    public float getClimateMaxScale() {
        NbtList base = this.getClimateBase();
        if(!base.isEmpty()) {
            NbtCompound compound = base.getCompound(1);
            if(compound.contains(MAX_BASE)) {
                return compound.getInt(MAX_BASE);
            }
        }
        return 0f;
    }

    public float getTotalPercent() {
        float lBaseLuck = this.getLocationPercentMaxLuck();
        float lBaseScale = this.getLocationPercentMaxScale();
        float cBaseLuck = this.getClimatePercentMaxLuck();
        float cBaseScale = this.getClimatePercentMaxScale();

        if(lBaseLuck != 0f || lBaseScale != 0f || cBaseLuck != 0f || cBaseScale != 0f) {
            return (lBaseLuck + lBaseScale + cBaseLuck + cBaseScale) / 4;
        }
        return 0f;
    }

    public static PetNbtObject of(@NotNull NbtCompound nbtCompound, @NotNull ItemStack itemStack) {
        return new PetNbtObject(nbtCompound, itemStack);
    }

    public static PetNbtObject empty() {
        return new PetNbtObject(ItemStackHelper.getNbt(ItemStack.EMPTY), ItemStack.EMPTY);
    }
}
