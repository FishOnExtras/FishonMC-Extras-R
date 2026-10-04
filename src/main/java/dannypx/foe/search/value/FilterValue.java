package dannypx.foe.search.value;

public sealed interface FilterValue permits NumberValue, StringValue, BooleanValue {
    static FilterValue of(Object raw) {
        return switch (raw) {
            case Number number -> new NumberValue(number);
            case String string -> new StringValue(string);
            case Boolean bool -> new BooleanValue(bool);
            case null, default -> null;
        };
    }

    static FilterValue bool(Boolean bool) {
        return new BooleanValue(bool);
    }

    static FilterValue number(Number number) {
        return new NumberValue(number);
    }

    static FilterValue string(String string) {
        return new StringValue(string);
    }
}
