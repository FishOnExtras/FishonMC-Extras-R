package dannypx.foe.search.editbox;

import dannypx.foe.helper.TextHelper;
import dannypx.foe.handler.logic.SearchHandler;
import dannypx.foe.search.field.SearchField;
import dannypx.foe.search.field.SearchFieldRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class SearchHelp {
    private static final int NAMES_PER_LINE = 4;

    private static final ChatFormatting FIELD = ChatFormatting.DARK_AQUA;
    private static final ChatFormatting OPERATOR = ChatFormatting.GOLD;
    private static final ChatFormatting VALUE = ChatFormatting.GREEN;
    private static final ChatFormatting DESCRIPTION = ChatFormatting.GRAY;

    private static List<Component> cached = List.of();
    private static int cachedFieldCount = -1;
    private static int cachedWidth = -1;

    private SearchHelp() {
    }

    public static List<Component> lines() {
        SearchFieldRegistry registry = SearchHandler.instance().getRegistry();
        int fieldCount = registry.all().size();

        if (fieldCount != cachedFieldCount) {
            cached = build(registry);
            cachedFieldCount = fieldCount;
            cachedWidth = -1;
        }
        return cached;
    }

    public static int width(Font font) {
        List<Component> lines = lines();
        if (cachedWidth < 0) {
            int widest = 0;
            for (Component line : lines) {
                widest = Math.max(widest, font.width(line));
            }
            cachedWidth = widest;
        }
        return cachedWidth;
    }

    private static List<Component> build(SearchFieldRegistry registry) {
        List<Component> lines = new ArrayList<>();

        lines.add(text("Item Search", ChatFormatting.WHITE, ChatFormatting.BOLD));
        lines.add(text("Type words to search item names. Add filters to compare fields with a value.", DESCRIPTION));
        lines.add(Component.empty());

        lines.add(heading("Filters"));
        lines.add(TextHelper.concat(
                text("field", FIELD),
                text("==", OPERATOR),
                text("value", VALUE),
                text(" or ", DESCRIPTION),
                text("field", FIELD),
                text("==", OPERATOR),
                text("\"value\"", VALUE)));
        lines.add(TextHelper.concat(
                text("Text: ", DESCRIPTION),
                text("==", OPERATOR), text(" or ", DESCRIPTION), text("=", OPERATOR), text(" contains, ", DESCRIPTION),
                text("!=", OPERATOR), text(" does not contain", DESCRIPTION)));
        lines.add(TextHelper.concat(text("Numbers: ", DESCRIPTION), text("==  !=  <  >  <=  >=", OPERATOR)));
        lines.add(TextHelper.concat(
                text("Boolean: ", DESCRIPTION),
                text("true", VALUE), text(" / ", DESCRIPTION), text("false", VALUE),
                text(" with ", DESCRIPTION), text("==", OPERATOR), text(" or ", DESCRIPTION), text("!=", OPERATOR)));
        lines.add(Component.empty());

        lines.add(heading("Modes"));
        lines.add(TextHelper.concat(text("Match All  ", VALUE, ChatFormatting.BOLD), text("(default) Every word and every filter must match.", DESCRIPTION)));
        lines.add(TextHelper.concat(text("Match Any  ", OPERATOR, ChatFormatting.BOLD), text("Start with ", DESCRIPTION), text("~", OPERATOR), text(". One matching word or filter is enough.", DESCRIPTION)));
        lines.add(Component.empty());

        lines.add(heading("Fields"));
        List<String> names = new ArrayList<>();
        for (SearchField field : registry.all()) {
            names.add(field.key());
        }
        for (int i = 0; i < names.size(); i += NAMES_PER_LINE) {
            lines.add(text(String.join("  ", names.subList(i, Math.min(i + NAMES_PER_LINE, names.size()))), FIELD));
        }
        lines.add(text("Any other key is read from the item's NBT.", DESCRIPTION));
        lines.add(text("Separate nested NBT keys with a dot.", DESCRIPTION));
        lines.add(Component.empty());

        lines.add(heading("Examples"));
        lines.add(TextHelper.concat(text("Pet Subtropical", ChatFormatting.WHITE), text("  name has both words", DESCRIPTION)));
        lines.add(TextHelper.concat(text("~", OPERATOR), text("Pet Subtropical", ChatFormatting.WHITE), text("  name has either word", DESCRIPTION)));
        lines.add(TextHelper.concat(text("tooltip", FIELD), text("==", OPERATOR), text("\"tunas\"", VALUE), text("  lore has tunas", DESCRIPTION)));
        lines.add(TextHelper.concat(
                text("rating", FIELD), text(">=", OPERATOR), text("95 ", VALUE),
                text("tooltip", FIELD), text("!=", OPERATOR), text("\"tunas\"", VALUE),
                text("  both must match", DESCRIPTION)));
        lines.add(TextHelper.concat(
                text("~", OPERATOR),
                text("tooltip", FIELD), text("==", OPERATOR), text("\"tunas\" ", VALUE),
                text("rating", FIELD), text("==", OPERATOR), text("100", VALUE),
                text("  either matches", DESCRIPTION)));

        return List.copyOf(lines);
    }

    private static MutableComponent heading(String title) {
        return text(title, ChatFormatting.WHITE, ChatFormatting.BOLD);
    }

    private static MutableComponent text(String value, ChatFormatting... formats) {
        return Component.literal(value).withStyle(formats);
    }
}
