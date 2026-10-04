package dannypx.foe.search.editbox;

import dannypx.foe.handler.logic.SearchHandler;
import dannypx.foe.search.field.SearchFieldRegistry;
import dannypx.foe.search.parser.SearchParser;
import dannypx.foe.search.query.SearchToken;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SearchHighlighter implements EditBox.TextFormatter {
    private static final Style PLAIN_STYLE = Style.EMPTY;
    private static final Style ACCENT_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);
    private static final Style FIELD_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_AQUA);
    private static final Style NBT_FIELD_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style VALUE_STYLE = Style.EMPTY.withColor(ChatFormatting.GREEN);

    private final EditBox box;

    private String cachedValue = "";
    private int cachedFieldCount = -1;
    private Style[] cachedStyles = new Style[0];

    public SearchHighlighter(EditBox box) {
        this.box = box;
    }

    @Override
    public @Nullable FormattedCharSequence format(String text, int start) {
        if (text.isEmpty()) return null;

        Style[] styles = styles();

        List<FormattedCharSequence> parts = new ArrayList<>();
        int runStart = 0;
        Style runStyle = styleAt(styles, start);
        for (int i = 1; i < text.length(); i++) {
            Style style = styleAt(styles, start + i);
            if (style != runStyle) {
                parts.add(FormattedCharSequence.forward(text.substring(runStart, i), runStyle));
                runStart = i;
                runStyle = style;
            }
        }
        parts.add(FormattedCharSequence.forward(text.substring(runStart), runStyle));

        return FormattedCharSequence.composite(parts);
    }

    private Style[] styles() {
        String value = box.getValue();
        SearchFieldRegistry registry = SearchHandler.instance().getRegistry();
        int fieldCount = registry.all().size();

        if (fieldCount != cachedFieldCount || !value.equals(cachedValue)) {
            Style[] styles = new Style[value.length()];
            Arrays.fill(styles, PLAIN_STYLE);

            for (SearchToken token : SearchParser.tokenize(value)) {
                Style style = switch (token.kind()) {
                    case MODE, OPERATOR -> ACCENT_STYLE;
                    case KEY -> registry.find(token.text()) != null ? FIELD_STYLE : NBT_FIELD_STYLE;
                    case VALUE -> VALUE_STYLE;
                };
                Arrays.fill(styles, token.start(), token.end(), style);
            }

            cachedValue = value;
            cachedFieldCount = fieldCount;
            cachedStyles = styles;
        }
        return cachedStyles;
    }

    private static Style styleAt(Style[] styles, int index) {
        return index >= 0 && index < styles.length ? styles[index] : PLAIN_STYLE;
    }
}
