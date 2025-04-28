package unit.info;

import java.awt.*;

public enum UnitCharacteristicValue {
    MAX(5), HIGH_MAX(4.5f), HIGH(4), GOOD_HIGH(3.5f), GOOD(3), MODERATE_GOOD(2.5f), MODERATE(2), LOW_MODERATE(1.5f), LOW(1), NONE_LOW(0.5f), NONE(0);
    public final float fill;

    UnitCharacteristicValue(float fill) {
        this.fill = fill;
    }

    public Color getDamageColour() {
        return switch (this) {
            case NONE -> new Color(92, 187, 228);
            case NONE_LOW -> new Color(210, 228, 92);
            case LOW -> new Color(228, 226, 92);
            case LOW_MODERATE -> new Color(228, 205, 92);
            case MODERATE -> new Color(228, 189, 92);
            case MODERATE_GOOD -> new Color(228, 165, 92);
            case GOOD -> new Color(228, 144, 92);
            case GOOD_HIGH -> new Color(221, 113, 80);
            case HIGH -> new Color(234, 102, 79);
            case HIGH_MAX -> new Color(230, 75, 75);
            case MAX -> new Color(223, 54, 54);
        };
    }
}
