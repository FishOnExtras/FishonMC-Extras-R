package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.search.editbox.SearchBarEditBox;
import dannypx.foe.search.field.SearchField;
import dannypx.foe.search.field.SearchFieldRegistry;
import dannypx.foe.search.match.SearchMatcher;
import dannypx.foe.search.parser.SearchParser;
import dannypx.foe.search.query.SearchFilter;
import dannypx.foe.search.query.SearchMode;
import dannypx.foe.search.query.SearchQuery;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class SearchHandler extends Handler {
    private static SearchHandler INSTANCE = new SearchHandler();

    public static SearchHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new SearchHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final SearchFieldRegistry registry = new SearchFieldRegistry();
    private SearchQuery query = SearchQuery.EMPTY;
    private String lastInput = "";
    private boolean isFocused = false;
    private boolean isOnScreen = false;

    private static final String CHECK = "✔";
    private static final String CROSS = "✘";

    private SearchHandler() {
        registry.init();
    }

    public SearchQuery getQuery() {
        return query;
    }

    public SearchMode getMode() {
        return query.mode();
    }

    public List<SearchFilter> getFilters() {
        return query.searchFilters();
    }

    public String getSearchRemainder() {
        return query.remainder();
    }

    public String getLastInput() {
        return lastInput;
    }

    public void setLastInput(String lastInput) {
        this.lastInput = lastInput;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
    }

    public boolean isOnScreen() {
        return isOnScreen;
    }

    public void setOnScreen(boolean onScreen) {
        isOnScreen = onScreen;
    }

    public SearchFieldRegistry getRegistry() {
        return registry;
    }
    //endregion

    //region Methods
    public void parseSearch(String input) {
        if (!Objects.equals(this.lastInput, input)) {
            this.lastInput = input;
            this.query = SearchParser.parse(input);
        }
    }

    public boolean filterItem(ItemStack itemStack) {
        return SearchMatcher.matches(query, registry, itemStack);
    }

    public SearchMatcher.Explanation explain(ItemStack itemStack) {
        return SearchMatcher.explain(query, registry, itemStack);
    }

    public List<Component> getMatchLines(ItemStack itemStack) {
        SearchMatcher.Explanation explanation = explain(itemStack);
        if (explanation.terms().isEmpty()) return List.of();

        List<Component> lines = new ArrayList<>();

        Component result = explanation.matched()
                ? Component.literal(CHECK + " Match").withStyle(ChatFormatting.GREEN)
                : Component.literal(CROSS + " No match").withStyle(ChatFormatting.RED);
        String modeText = explanation.mode() == SearchMode.ANY ? " (any term)" : " (all terms)";
        lines.add(Component.empty()
                .append(Component.literal("Search: ").withStyle(ChatFormatting.GRAY))
                .append(result)
                .append(Component.literal(modeText).withStyle(ChatFormatting.DARK_GRAY)));

        for (SearchMatcher.Term term : explanation.terms()) {
            lines.add(Component.empty()
                    .append(Component.literal(term.matched() ? CHECK : CROSS)
                            .withStyle(term.matched() ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(Component.literal(" " + term.text()).withStyle(ChatFormatting.GRAY)));
        }
        return lines;
    }

    public void registerField(SearchField field) {
        registry.register(field);
    }

    public void registerNumberField(String key, String description, Function<ItemStack, ? extends Number> source) {
        registry.register(SearchField.of(key).withNumber(ctx -> source.apply(ctx.stack())).description(description));
    }

    public void registerStringField(String key, String description, Function<ItemStack, String> source) {
        registry.register(SearchField.of(key).withString(ctx -> source.apply(ctx.stack())).description(description));
    }

    public void registerBooleanField(String key, String description, Function<ItemStack, Boolean> source) {
        registry.register(SearchField.of(key).withBoolean(ctx -> source.apply(ctx.stack())).description(description));
    }

    public static SearchBarEditBox getSearchBar(int x, int y, int width, int height) {
        return new SearchBarEditBox(minecraft.font, x, y, width, height);
    }
    //endregion

    //region Dev

    /// Field, Pair<Value, Tooltip>
    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "key", Pair.of(Component.literal("value"), Component.empty())
        );
    }
    //endregion
}
