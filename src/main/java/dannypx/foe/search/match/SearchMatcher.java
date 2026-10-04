package dannypx.foe.search.match;

import dannypx.foe.search.field.SearchContext;
import dannypx.foe.search.field.SearchField;
import dannypx.foe.search.field.SearchFieldRegistry;
import dannypx.foe.search.field.SearchNbtResolver;
import dannypx.foe.search.query.SearchFilter;
import dannypx.foe.search.query.SearchMode;
import dannypx.foe.search.query.SearchQuery;
import dannypx.foe.search.value.FilterValue;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SearchMatcher {
    public record Term(String text, boolean matched) {
    }

    public record Explanation(boolean matched, SearchMode mode, List<Term> terms) {
    }

    public static boolean matches(SearchQuery query, SearchFieldRegistry registry, ItemStack stack) {
        if (query.isEmpty()) return false;

        SearchContext ctx = new SearchContext(stack);
        if (ctx.tooltipHidden()) return false;

        boolean all = query.mode() == SearchMode.ALL;

        for (String word : query.words()) {
            boolean hit = ctx.lowerName().contains(word);
            if (all && !hit) return false;
            if (!all && hit) return true;
        }

        for (SearchFilter filter : query.searchFilters()) {
            boolean hit = testFilter(filter, ctx, registry);
            if (all && !hit) return false;
            if (!all && hit) return true;
        }

        return all;
    }

    private static boolean testFilter(SearchFilter filter, SearchContext ctx, SearchFieldRegistry registry) {
        SearchField field = registry.find(filter.key());
        if (field != null) {
            return field.matches(ctx, filter.operator(), filter.value());
        }

        FilterValue actual = SearchNbtResolver.resolve(ctx, filter.key());
        return SearchComparator.compare(actual, filter.operator(), filter.value());
    }

    public static Explanation explain(SearchQuery query, SearchFieldRegistry registry, ItemStack stack) {
        if (query.isEmpty()) return new Explanation(false, query.mode(), List.of());

        SearchContext ctx = new SearchContext(stack);

        List<Term> terms = new ArrayList<>();
        for (String word : query.words()) {
            terms.add(new Term(word, ctx.lowerName().contains(word)));
        }
        for (SearchFilter filter : query.searchFilters()) {
            terms.add(new Term(filter.display(), testFilter(filter, ctx, registry)));
        }

        boolean matched = query.mode() == SearchMode.ALL
                ? terms.stream().allMatch(Term::matched)
                : terms.stream().anyMatch(Term::matched);

        return new Explanation(matched && !ctx.tooltipHidden(), query.mode(), List.copyOf(terms));
    }
}
