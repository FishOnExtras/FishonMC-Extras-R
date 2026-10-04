package dannypx.foe.search.query;

import java.util.List;

public record SearchQuery(SearchMode mode, List<String> words, List<SearchFilter> searchFilters) {
    public static final SearchQuery EMPTY = new SearchQuery(SearchMode.ALL, List.of(), List.of());

    public SearchQuery {
        words = List.copyOf(words);
        searchFilters = List.copyOf(searchFilters);
    }

    public boolean isEmpty() {
        return words().isEmpty() && searchFilters.isEmpty();
    }

    public String remainder() {
        return String.join(" ", words);
    }
}
