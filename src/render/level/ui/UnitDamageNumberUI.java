package render.level.ui;

import java.awt.*;

import static level.energy.EnergyManager.*;

public class UnitDamageNumberUI extends UnitTextUI {
    private static final Color SHIELD_COLOR = new Color(97, 204, 216);

    public UnitDamageNumberUI(float damage, float x, float y, float moveFactor, boolean shield, float time) {
        super(numberText(damage, 1), x, y, 0.7f, moveFactor, shield ? SHIELD_COLOR : numberColour(damage), time);
    }
}
