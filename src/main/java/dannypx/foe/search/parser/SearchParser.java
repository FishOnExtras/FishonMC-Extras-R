package dannypx.foe.search.parser;

import dannypx.foe.search.query.SearchFilter;
import dannypx.foe.search.query.SearchMode;
import dannypx.foe.search.query.SearchQuery;
import dannypx.foe.search.query.SearchToken;
import dannypx.foe.search.value.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SearchParser {
    private static final Pattern FILTER_PATTERN = buildPattern();

    public static SearchQuery parse(String input) {
        String text = input.stripLeading();

        SearchMode mode = SearchMode.ALL;
        if(text.startsWith("~")) {
            mode = SearchMode.ANY;
            text = text.substring(1);
        }

        List<SearchFilter> filters = new ArrayList<>();
        Matcher matcher = FILTER_PATTERN.matcher(text);
        while(matcher.find()) {
            Operator operator = Operator.fromSymbol(matcher.group(2));
            FilterValue value = parseValue(matcher);
            if(operator != null && value != null) {
                filters.add(new SearchFilter(matcher.group(1), operator, value));
            }
        }

        String remainder = matcher.replaceAll(" ");
        List<String> words = new ArrayList<>();
        for (String word : remainder.trim().split("\\s+")) {
            if(!word.isEmpty()) {
                words.add(word.toLowerCase(Locale.US));
            }
        }

        return new SearchQuery(mode, words, filters);
    }

    public static List<SearchToken> tokenize(String input) {
        List<SearchToken> tokens = new ArrayList<>();

        int base = input.length() - input.stripLeading().length();
        String text = input.substring(base);
        if (text.startsWith("~")) {
            tokens.add(new SearchToken(base, base + 1, SearchToken.Kind.MODE, "~"));
            base++;
            text = text.substring(1);
        }

        Matcher matcher = FILTER_PATTERN.matcher(text);
        while (matcher.find()) {
            tokens.add(new SearchToken(base + matcher.start(1), base + matcher.end(1), SearchToken.Kind.KEY, matcher.group(1)));
            tokens.add(new SearchToken(base + matcher.start(2), base + matcher.end(2), SearchToken.Kind.OPERATOR, matcher.group(2)));

            int valueStart;
            if (matcher.start(3) >= 0) valueStart = matcher.start(3) - 1;
            else if (matcher.start(4) >= 0) valueStart = matcher.start(4);
            else valueStart = matcher.start(5);
            tokens.add(new SearchToken(base + valueStart, base + matcher.end(), SearchToken.Kind.VALUE, ""));
        }

        return tokens;
    }

    private static FilterValue parseValue(Matcher matcher) {
        if (matcher.group(3) != null) {
            return new StringValue(matcher.group(3));
        }
        if (matcher.group(4) != null) {
            return new NumberValue(Double.valueOf(matcher.group(4)));
        }

        String word = matcher.group(5);
        if (word == null) return null;
        if (word.equalsIgnoreCase("true")) return new BooleanValue(true);
        if (word.equalsIgnoreCase("false")) return new BooleanValue(false);
        return new StringValue(word);
    }

    private static Pattern buildPattern() {
        String operators = Arrays.stream(Operator.values())
                .map(operator -> operator.symbol)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        return Pattern.compile("(\\w+(?:\\.\\w+)*)\\s*(" + operators + ")\\s*(?:\"([^\"]+)\"|(-?\\d+(?:\\.\\d+)?)|([A-Za-z_]\\w*))");
    }
}
