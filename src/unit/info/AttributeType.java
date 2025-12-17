package unit.info;

import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.input.button.UIShapeButton;

import java.awt.*;
import java.util.function.Function;

public enum AttributeType {
    POSITIVE(UIColourTheme.DEEP_GREEN, UIShapeButton::plus), NEUTRAL(UIColourTheme.DEEP_YELLOW, UIShapeButton::dot), NEGATIVE(UIColourTheme.DEEP_RED, UIShapeButton::minus);

    public final UIColourTheme colour;
    public final Function<UIBox, Shape> shapeFunction;

    AttributeType(UIColourTheme theme, Function<UIBox, Shape> shapeFunction) {
        colour = theme;
        this.shapeFunction = shapeFunction;
    }
}
