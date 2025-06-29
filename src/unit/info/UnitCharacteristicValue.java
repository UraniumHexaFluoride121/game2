package unit.info;

public enum UnitCharacteristicValue {
    MAX(5), HIGH_MAX(4.5f), HIGH(4), GOOD_HIGH(3.5f), GOOD(3), MODERATE_GOOD(2.5f), MODERATE(2), LOW_MODERATE(1.5f), LOW(1), NONE_LOW(0.5f), NONE(0);
    public final float fill;

    UnitCharacteristicValue(float fill) {
        this.fill = fill;
    }
}
