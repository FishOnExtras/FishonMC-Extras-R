package dannypx.foe.search.query;

import dannypx.foe.search.value.*;

import java.math.BigDecimal;

public record SearchFilter(String key, Operator operator, FilterValue value) {
    public String display() {
        String valueText = switch (value) {
            case StringValue string -> "\"" + string.value() + "\"";
            case NumberValue number -> number.value() instanceof BigDecimal decimal
                    ? decimal.toPlainString()
                    : number.value().toString();
            case BooleanValue bool -> String.valueOf(bool.value());
        };
        return key + operator.symbol + valueText;
    }
}
