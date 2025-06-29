package render;

import java.awt.*;
import java.util.function.UnaryOperator;

public class UIColourTheme {
    public static UIColourTheme LIGHT_BLUE = new UIColourTheme(
            new Color(105, 181, 228), new Color(87, 142, 189, 144)
    );
    public static UIColourTheme GREEN_SELECTED = new UIColourTheme(
            new Color(105, 181, 228), new Color(87, 142, 189, 144),
            new Color(125, 195, 220), new Color(92, 140, 181, 133),
            new Color(113, 184, 148), new Color(74, 159, 108, 115),
            new Color(99, 193, 93), new Color(88, 175, 105, 128)
    );
    public static UIColourTheme ALWAYS_GREEN_SELECTED = new UIColourTheme(
            new Color(99, 193, 93), new Color(88, 175, 105, 128),
            new Color(90, 175, 84), new Color(71, 145, 86, 128),
            new Color(82, 165, 78), new Color(75, 151, 90, 128),
            new Color(99, 193, 93), new Color(88, 175, 105, 128)
    );
    public static UIColourTheme GREEN_SELECTED_TAB = new UIColourTheme(
            new Color(92, 166, 211), new Color(87, 142, 189, 144),
            new Color(99, 170, 191), new Color(92, 140, 181, 133),
            new Color(75, 147, 109), new Color(74, 159, 108, 115),
            new Color(99, 193, 93), new Color(88, 175, 105, 128)
    );
    public static UIColourTheme LIGHT_BLUE_TRANSPARENT_CENTER = new UIColourTheme(
            new Color(105, 181, 228), new Color(87, 142, 189, 71)
    );
    public static UIColourTheme LIGHT_BLUE_BOX_DARK = new UIColourTheme(
            new Color(105, 181, 228), new Color(27, 51, 66, 239)
    );
    public static UIColourTheme GREEN_SELECTED_OPAQUE_CENTER = new UIColourTheme(
            new Color(105, 181, 228), new Color(27, 51, 66, 239),
            new Color(99, 156, 191), new Color(25, 43, 55, 239),
            new Color(75, 147, 100), new Color(20, 44, 30, 239),
            new Color(90, 166, 81), new Color(23, 43, 19, 239)
    );
    public static UIColourTheme LIGHT_BLUE_BOX = new UIColourTheme(
            new Color(105, 181, 228), new Color(35, 66, 87, 239)
    );
    public static UIColourTheme LIGHT_BLUE_FULLY_OPAQUE_CENTER = new UIColourTheme(
            new Color(105, 181, 228), new Color(27, 51, 66)
    );



    public static UIColourTheme GRAYED_OUT = UIColourTheme.useSameColourForAllStates(
            new Color(161, 161, 161), new Color(131, 131, 131, 144)
    );
    public static UIColourTheme GRAYED_OUT_OPAQUE = UIColourTheme.useSameColourForAllStates(
            new Color(161, 161, 161), new Color(76, 76, 76, 219)
    );
    public static UIColourTheme DARK_GRAY = new UIColourTheme(
            new Color(97, 97, 97), new Color(69, 69, 69, 221)
    );
    public static UIColourTheme DEEP_LIGHT_BLUE = new UIColourTheme(
            new Color(68, 157, 220), new Color(58, 152, 193, 144)
    );
    public static UIColourTheme DEEP_GREEN = new UIColourTheme(
            new Color(106, 220, 68), new Color(92, 193, 58, 144)
    );
    public static UIColourTheme DEEP_YELLOW = new UIColourTheme(
            new Color(220, 185, 68), new Color(193, 171, 58, 144)
    );
    public static UIColourTheme DEEP_RED = new UIColourTheme(
            new Color(220, 68, 68), new Color(193, 58, 58, 144)
    );



    public static UIColourTheme RED = new UIColourTheme(
            new Color(228, 105, 105), new Color(189, 87, 87, 144)
    );
    public static UIColourTheme GREEN = new UIColourTheme(
            new Color(121, 228, 105), new Color(101, 189, 87, 144)
    );
    public static UIColourTheme BLUE = new UIColourTheme(
            new Color(85, 121, 211), new Color(87, 111, 189, 144)
    );
    public static UIColourTheme YELLOW = new UIColourTheme(
            new Color(228, 208, 105), new Color(189, 169, 87, 144)
    );
    public static UIColourTheme ORANGE = new UIColourTheme(
            new Color(228, 156, 105), new Color(189, 123, 87, 144)
    );
    public static UIColourTheme RED_UNAVAILABLE = UIColourTheme.useSameColourForAllStates(
            new Color(135, 87, 87), new Color(145, 92, 92, 144)
    );
    public static UIColourTheme GREEN_UNAVAILABLE = UIColourTheme.useSameColourForAllStates(
            new Color(114, 152, 111), new Color(107, 145, 93, 144)
    );
    public static UIColourTheme BLUE_UNAVAILABLE = UIColourTheme.useSameColourForAllStates(
            new Color(87, 95, 135), new Color(92, 99, 145, 144)
    );
    public static UIColourTheme YELLOW_UNAVAILABLE = UIColourTheme.useSameColourForAllStates(
            new Color(161, 153, 98), new Color(163, 149, 100, 144)
    );

    public final Color borderColour;
    public final Color backgroundColour;
    public final Color borderColourHover;
    public final Color backgroundColourHover;
    public final Color borderColourPressed;
    public final Color backgroundColourPressed;
    public final Color borderColourSelected;
    public final Color backgroundColourSelected;

    public UIColourTheme(Color borderColour, Color backgroundColour, Color borderColourHover, Color backgroundColourHover,
                  Color borderColourPressed, Color backgroundColourPressed, Color borderColourSelected, Color backgroundColourSelected) {
        this.borderColour = borderColour;
        this.backgroundColour = backgroundColour;
        this.borderColourHover = borderColourHover;
        this.backgroundColourHover = backgroundColourHover;
        this.borderColourPressed = borderColourPressed;
        this.backgroundColourPressed = backgroundColourPressed;
        this.borderColourSelected = borderColourSelected;
        this.backgroundColourSelected = backgroundColourSelected;
    }

    public UIColourTheme(Color borderColour, Color backgroundColour) {
        this.borderColour = borderColour;
        this.backgroundColour = backgroundColour;
        this.borderColourHover = darken(borderColour, 0.86f);
        this.backgroundColourHover = darken(backgroundColour, 0.95f);
        this.borderColourPressed = darken(borderColour, 0.7f);
        this.backgroundColourPressed = darken(backgroundColour, 0.85f);
        this.borderColourSelected = darken(borderColour, 0.75f);
        this.backgroundColourSelected = darken(backgroundColour, 0.8f);
    }

    public static UIColourTheme useSameColourForAllStates(Color borderColour, Color backgroundColour) {
        return new UIColourTheme(
                borderColour, backgroundColour,
                borderColour, backgroundColour,
                borderColour, backgroundColour,
                borderColour, backgroundColour
        );
    }

    public UIColourTheme backgroundModifier(UnaryOperator<Color> modifier) {
        return new UIColourTheme(borderColour, modifier.apply(backgroundColour),
                borderColourHover, modifier.apply(backgroundColourHover),
                borderColourPressed, modifier.apply(backgroundColourPressed),
                borderColourSelected, modifier.apply(backgroundColourSelected));
    }

    public static Color applyAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.clamp((int) (c.getAlpha() * alpha), 0, 255));
    }

    public static Color setAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * alpha));
    }

    public static Color darken(Color c, float brightness) {
        return new Color((int) (c.getRed() * brightness), (int) (c.getGreen() * brightness), (int) (c.getBlue() * brightness), c.getAlpha());
    }
}
