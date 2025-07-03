package render.types.text;

import java.awt.*;

public class TextStyle {
    public Color colour = null;

    public TextStyle modify(String modifier) {
        for (StyleElement value : StyleElement.values()) {
            if (modifier.equals(value.name()))
                return value.modifier.apply(value, copy());
        }
        throw new IllegalArgumentException("Style modifier \"" + modifier + "\" is not valid");
    }

    public TextStyle setColour(Color colour) {
        this.colour = colour;
        return this;
    }

    public static TextStyle setColour(StyleElement modifier, TextStyle style) {
        return style.setColour(modifier.colour);
    }

    public TextStyle copy() {
        return new TextStyle().setColour(colour);
    }
}
