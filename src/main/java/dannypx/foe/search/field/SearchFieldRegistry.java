package dannypx.foe.search.field;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SearchFieldRegistry {
    private final Map<String, SearchField> byName = new HashMap<>();
    private final List<SearchField> fields = new ArrayList<>();

    public void init() {
        register(of("type")
                .withString(SearchContext::itemType)
                .description("Item type"));

        register(of("tooltip")
                .withString(SearchContext::tooltip)
                .description("Any lore line"));

        register(of("rating")
                .alias("pet_rating")
                .withNumber(SearchContext::ratingPercent)
                .withString(SearchContext::ratingText)
                .description("Pet rating in percent, or the rating text"));

        register(of("lluck")
                .withNumber(SearchContext::locationLuck)
                .description("Location luck"));
        register(of("lscale")
                .withNumber(SearchContext::locationScale)
                .description("Location scale"));
        register(of("cluck")
                .withNumber(SearchContext::climateLuck)
                .description("Climate luck"));
        register(of("cscale")
                .withNumber(SearchContext::climateScale)
                .description("Climate scale"));

        register(of("lluck_percent")
                .withNumber(SearchContext::locationLuckPercent)
                .description("Location luck in percent"));
        register(of("lscale_percent")
                .withNumber(SearchContext::locationScalePercent)
                .description("Location scale in percent"));
        register(of("cluck_percent")
                .withNumber(SearchContext::climateLuckPercent)
                .description("Climate luck in percent"));
        register(of("cscale_percent")
                .withNumber(SearchContext::climateScalePercent)
                .description("Climate scale in percent"));
    }

    public void register(SearchField field) {
        for (String name : field.names()) {
            if (byName.containsKey(name)) {
                throw new IllegalArgumentException("Search field name already registered: " + name);
            }
        }
        fields.add(field);
        for (String name : field.names()) {
            byName.put(name, field);
        }
    }

    public @Nullable SearchField find(String key) {
        return byName.get(key.toLowerCase(Locale.US));
    }

    public List<SearchField> all() {
        return Collections.unmodifiableList(fields);
    }

    private static SearchField of(String key) {
        return SearchField.of(key);
    }
}
