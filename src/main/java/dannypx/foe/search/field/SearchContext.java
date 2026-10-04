package dannypx.foe.search.field;

import dannypx.foe.helper.TextHelper;
import dannypx.foe.item.PetTagObject;
import dannypx.foe.item.TagObject;
import dannypx.foe.item.ValidateItem;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SearchContext {
    private final ItemStack stack;

    private @Nullable String lowerName;

    private boolean petResolved;
    private @Nullable PetTagObject pet;

    private boolean typeResolved;
    private @Nullable String itemType;

    private boolean serverTagResolved;
    private @Nullable TagObject serverTag;

    private boolean tooltipResolved;
    private @Nullable String tooltipText;

    public SearchContext(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack stack() {
        return stack;
    }

    public boolean tooltipHidden() {
        var display = stack.get(DataComponents.TOOLTIP_DISPLAY);
        return display != null && display.hideTooltip();
    }

    public String lowerName() {
        if (lowerName == null) {
            lowerName = stack.getHoverName().getString().toLowerCase(Locale.US);
        }
        return lowerName;
    }

    public @Nullable PetTagObject pet() {
        if (!petResolved) {
            petResolved = true;
            Pair<Boolean, PetTagObject> result = ValidateItem.isPet(stack);
            pet = result.value1() ? result.value2() : null;
        }
        return pet;
    }

    public @Nullable TagObject serverTag() {
        if (!serverTagResolved) {
            serverTagResolved = true;
            Pair<Boolean, TagObject> result = ValidateItem.isServerItem(stack, true);
            serverTag = result.value1() ? result.value2() : null;
        }
        return serverTag;
    }

    public @Nullable String itemType() {
        if (!typeResolved) {
            typeResolved = true;
            Pair<Boolean, TagObject> result = ValidateItem.isType(stack);
            if (result.value1()) {
                itemType = result.value2().getType();
            }
        }
        return itemType;
    }

    public @Nullable String tooltip() {
        if (!tooltipResolved) {
            tooltipResolved = true;
            var lore = stack.get(DataComponents.LORE);
            if (lore != null) {
                List<String> lines = new ArrayList<>();
                for (Component line : lore.lines()) {
                    lines.add(TextHelper.normalLetter(line.getString()).toLowerCase(Locale.US));
                }
                tooltipText = String.join("\n", lines);
            }
        }
        return tooltipText;
    }

    public @Nullable Number ratingPercent() {
        PetTagObject pet = pet();
        return pet == null ? null : percent(pet.getTotalPercent());
    }

    public @Nullable String ratingText() {
        PetTagObject pet = pet();
        return pet == null ? null : TextHelper.normalLetter(pet.getRatingComponent().getString());
    }

    public @Nullable Number locationLuck() {
        PetTagObject pet = pet();
        return pet == null ? null : pet.getLocationMaxLuck();
    }

    public @Nullable Number locationScale() {
        PetTagObject pet = pet();
        return pet == null ? null : pet.getLocationMaxScale();
    }

    public @Nullable Number climateLuck() {
        PetTagObject pet = pet();
        return pet == null ? null : pet.getClimateMaxLuck();
    }

    public @Nullable Number climateScale() {
        PetTagObject pet = pet();
        return pet == null ? null : pet.getClimateMaxScale();
    }

    public @Nullable Number locationLuckPercent() {
        PetTagObject pet = pet();
        return pet == null ? null : percent(pet.getLocationPercentMaxLuck());
    }

    public @Nullable Number locationScalePercent() {
        PetTagObject pet = pet();
        return pet == null ? null : percent(pet.getLocationPercentMaxScale());
    }

    public @Nullable Number climateLuckPercent() {
        PetTagObject pet = pet();
        return pet == null ? null : percent(pet.getClimatePercentMaxLuck());
    }

    public @Nullable Number climateScalePercent() {
        PetTagObject pet = pet();
        return pet == null ? null : percent(pet.getClimatePercentMaxScale());
    }

    private static @Nullable Number percent(Number fraction) {
        try {
            return new BigDecimal(fraction.toString()).movePointRight(2);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
