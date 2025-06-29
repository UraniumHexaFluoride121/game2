package level.energy;

import render.HorizontalAlign;
import render.UIColourTheme;
import render.types.box.UIDisplayBox;

public class EnergyDisplay extends UIDisplayBox {
    public EnergyDisplay(float width, boolean dynamicWidth) {
        this(width, 1, dynamicWidth);
    }

    public EnergyDisplay(float width, float height, boolean dynamicWidth) {
        super(0, 0, width, height, box -> box.setCorner(.5f).setColourTheme(UIColourTheme.DARK_GRAY), dynamicWidth);
        addText(0.6f, HorizontalAlign.CENTER, null);
        setHorizontalAlign(HorizontalAlign.CENTER);
    }

    public EnergyDisplay setText(String s) {
        super.setText(s);
        return this;
    }
}
