package dannypx.foe.search.value;

public enum Operator {
    EQUAL("=="),
    SHORT_EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    GREATER(">"),
    LESS("<");

    public final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public static Operator fromSymbol(String symbol) {
        for (Operator op : values()) {
            if (op.symbol.equals(symbol)) return op;
        }
        return null;
    }
}
