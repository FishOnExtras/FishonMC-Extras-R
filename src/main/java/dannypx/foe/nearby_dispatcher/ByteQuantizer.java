package dannypx.foe.nearby_dispatcher;

public class ByteQuantizer {
    private final int minimumStep;
    private final int maximumStep;
    private final int stepsPerUnit;

    public ByteQuantizer(float minimum, float maximum, int stepsPerUnit) {
        this.stepsPerUnit = stepsPerUnit;
        this.minimumStep = Math.round(minimum * stepsPerUnit);
        this.maximumStep = Math.round(maximum * stepsPerUnit);
        if (maximumStep <= minimumStep || maximumStep - minimumStep > 255) {
            throw new IllegalArgumentException("range must span 1-255 steps, got " + (maximumStep - minimumStep));
        }
    }

    public byte encode(float value) {
        int step = Math.clamp(Math.round(value * stepsPerUnit), minimumStep, maximumStep);
        return (byte) (step - minimumStep);
    }

    public float decode(byte encodedValue) {
        int stepIndex = Math.min(encodedValue & 0xFF, maximumStep - minimumStep);
        return (minimumStep + stepIndex) / (float) stepsPerUnit;
    }
}
