package dannypx.foe.search.query;

public record SearchToken(int start, int end, Kind kind, String text) {
    public enum Kind {
        MODE,
        KEY,
        OPERATOR,
        VALUE
    }
}
