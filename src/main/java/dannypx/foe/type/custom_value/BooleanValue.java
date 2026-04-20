package dannypx.foe.type.custom_value;

public record BooleanValue(boolean value) implements TrackerValue {
    @Override
    public TrackerValue setValue(boolean value) {
        return new BooleanValue(value);
    }

    @Override
    public TrackerValue toggleValue() {
        return new BooleanValue(!this.value);
    }

    public static TrackerValue getDefault() {
        return new BooleanValue(false);
    }
}
