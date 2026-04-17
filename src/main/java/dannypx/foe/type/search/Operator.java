package dannypx.foe.type.search;

public enum Operator {
    SEPARATOR(","),

    EQUAL("=="),
    SHORT_EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    GREATER(">"),
    LESS("<"),

    ADDITION("\\+"),
    SUBTRACTION("\\-"),
    MULTIPLICATION("\\*"),
    DIVISION("\\/"),
    MODULO("\\%"),
    POWER("\\^")
    ;

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
