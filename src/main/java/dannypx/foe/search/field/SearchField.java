package dannypx.foe.search.field;

import dannypx.foe.search.match.SearchComparator;
import dannypx.foe.search.value.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public final class SearchField {
    private final String key;
    private final Set<String> names = new LinkedHashSet<>();
    private String description = "";

    private @Nullable Function<SearchContext, ? extends Number> numberSource;
    private @Nullable Function<SearchContext, String> stringSource;
    private @Nullable Function<SearchContext, Boolean> booleanSource;

    private SearchField(String key) {
        this.key = key;
        this.names.add(key.toLowerCase(Locale.US));
    }

    public static SearchField of(String key) {
        return new SearchField(key);
    }

    public SearchField withNumber(Function<SearchContext, ? extends Number> source) {
        this.numberSource = source;
        return this;
    }

    public SearchField withString(Function<SearchContext, String> source) {
        this.stringSource = source;
        return this;
    }

    public SearchField withBoolean(Function<SearchContext, Boolean> source) {
        this.booleanSource = source;
        return this;
    }

    public SearchField alias(String... aliases) {
        for (String alias : aliases) {
            names.add(alias.toLowerCase(Locale.US));
        }
        return this;
    }

    public SearchField description(String description) {
        this.description = description;
        return this;
    }
    public String key() {
        return key;
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(names);
    }

    public String description() {
        return description;
    }

    public boolean matches(SearchContext ctx, Operator operator, FilterValue expected) {
        FilterValue actual = switch (expected) {
            case NumberValue ignored -> numberSource == null ? null : FilterValue.of(numberSource.apply(ctx));
            case StringValue ignored -> stringSource == null ? null : FilterValue.of(stringSource.apply(ctx));
            case BooleanValue ignored -> booleanSource == null ? null : FilterValue.of(booleanSource.apply(ctx));
        };

        return SearchComparator.compare(actual, operator, expected);
    }
}
