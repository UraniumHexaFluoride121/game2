package render.types.text;

import java.awt.*;
import java.util.function.UnaryOperator;

public class TextStyle {
    public Color colour = null;

    public TextStyle modify(String modifier) {
        for (StyleElement value : StyleElement.values()) {
            if (modifier.equals(value.name()))
                return value.modifier.apply(copy());
        }
        throw new IllegalArgumentException("Style modifier \"" + modifier + "\" is not valid");
    }

    public TextStyle setColour(Color colour) {
        this.colour = colour;
        return this;
    }

    public TextStyle copy() {
        return new TextStyle().setColour(colour);
    }

    private enum StyleElement {
        NO_COLOUR(s -> s.setColour(null)),
        BLUE(s -> s.setColour(new Color(46, 151, 220))),
        GREEN(s -> s.setColour(new Color(57, 218, 59))),
        DARK_GREEN(s -> s.setColour(new Color(40, 113, 42))),
        YELLOW(s -> s.setColour(new Color(218, 186, 57))),
        ORANGE(s -> s.setColour(new Color(218, 124, 57))),
        RESUPPLY(s -> s.setColour(new Color(161, 101, 37))),
        RED(s -> s.setColour(new Color(216, 70, 70)));

        public final UnaryOperator<TextStyle> modifier;

        StyleElement(UnaryOperator<TextStyle> modifier) {
            this.modifier = modifier;
        }
    }
}
