package dannypx.foe.search.match;

import dannypx.foe.search.value.*;

import java.math.BigDecimal;
import java.util.Locale;

public final class SearchComparator {
    public static boolean compare(FilterValue actual, Operator operator, FilterValue expected) {
        if (actual instanceof StringValue(String left) && expected instanceof StringValue(String right)) {
            return compareStrings(left, operator, right);
        }
        if (actual instanceof NumberValue(Number left) && expected instanceof NumberValue(Number right)) {
            return compareNumbers(left, operator, right);
        }
        if (actual instanceof BooleanValue(boolean left) && expected instanceof BooleanValue(boolean right)) {
            return compareBooleans(left, operator, right);
        }
        return false;
    }

    private static boolean compareStrings(String actual, Operator operator, String expected) {
        boolean contains = actual.toLowerCase(Locale.US).contains(expected.toLowerCase(Locale.US));
        return switch (operator) {
            case EQUAL, SHORT_EQUAL -> contains;
            case NOT_EQUAL -> !contains;
            case GREATER_EQUAL, LESS_EQUAL, GREATER, LESS -> false;
        };
    }

    private static boolean compareNumbers(Number actual, Operator operator, Number expected) {
        BigDecimal a = toDecimal(actual);
        BigDecimal e = toDecimal(expected);
        if (a == null || e == null) return false;

        int cmp = a.compareTo(e);
        return switch (operator) {
            case EQUAL, SHORT_EQUAL -> cmp == 0;
            case NOT_EQUAL -> cmp != 0;
            case GREATER_EQUAL -> cmp >= 0;
            case LESS_EQUAL -> cmp <= 0;
            case GREATER -> cmp > 0;
            case LESS -> cmp < 0;
        };
    }

    private static boolean compareBooleans(boolean actual, Operator operator, boolean expected) {
        return switch (operator) {
            case EQUAL, SHORT_EQUAL -> actual == expected;
            case NOT_EQUAL -> actual != expected;
            case GREATER_EQUAL, LESS_EQUAL, GREATER, LESS -> false;
        };
    }

    private static BigDecimal toDecimal(Number number) {
        if (number instanceof BigDecimal decimal) return decimal;
        try {
            return new BigDecimal(number.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
