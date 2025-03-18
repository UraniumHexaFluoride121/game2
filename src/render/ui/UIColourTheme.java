package render.ui;

import java.awt.*;

public enum UIColourTheme {
    LIGHT_BLUE(
            new Color(105, 181, 228), new Color(87, 142, 189, 144),
            new Color(99, 156, 191), new Color(92, 140, 181, 133),
            new Color(75, 121, 147), new Color(74, 121, 159, 115),
            new Color(81, 135, 166), new Color(88, 137, 175, 128)
    ),
    GRAYED_OUT(
            new Color(161, 161, 161), new Color(131, 131, 131, 144),
            new Color(161, 161, 161), new Color(131, 131, 131, 144),
            new Color(161, 161, 161), new Color(131, 131, 131, 144),
            new Color(161, 161, 161), new Color(131, 131, 131, 144)
    ),
    GRAYED_OUT_OPAQUE(
            new Color(161, 161, 161), new Color(76, 76, 76, 219),
            new Color(161, 161, 161), new Color(76, 76, 76, 219),
            new Color(161, 161, 161), new Color(76, 76, 76, 219),
            new Color(161, 161, 161), new Color(76, 76, 76, 219)
    ),
    DARK_GRAY(
            new Color(97, 97, 97), new Color(69, 69, 69, 221),
            new Color(97, 97, 97), new Color(69, 69, 69, 221),
            new Color(97, 97, 97), new Color(69, 69, 69, 221),
            new Color(97, 97, 97), new Color(69, 69, 69, 221)
    ),
    GREEN_SELECTED(
            new Color(105, 181, 228), new Color(87, 142, 189, 144),
            new Color(125, 195, 220), new Color(92, 140, 181, 133),
            new Color(113, 184, 148), new Color(74, 159, 108, 115),
            new Color(99, 193, 93), new Color(88, 175, 105, 128)
    ),
    ALWAYS_GREEN_SELECTED(
            new Color(99, 193, 93), new Color(88, 175, 105, 128),
            new Color(90, 175, 84), new Color(71, 145, 86, 128),
            new Color(82, 165, 78), new Color(75, 151, 90, 128),
            new Color(99, 193, 93), new Color(88, 175, 105, 128)
    ),
    GREEN_SELECTED_TAB(
            new Color(92, 166, 211), new Color(87, 142, 189, 144),
            new Color(99, 170, 191), new Color(92, 140, 181, 133),
            new Color(75, 147, 109), new Color(74, 159, 108, 115),
            new Color(99, 193, 93), new Color(88, 175, 105, 128)
    ),
    DEEP_GREEN(
            new Color(106, 220, 68), new Color(92, 193, 58, 144),
            new Color(106, 193, 71), new Color(82, 184, 54, 133),
            new Color(64, 145, 41), new Color(79, 163, 46, 115),
            new Color(76, 172, 49), new Color(74, 179, 51, 128)
    ),
    DEEP_YELLOW(
            new Color(220, 185, 68), new Color(193, 171, 58, 144),
            new Color(193, 163, 71), new Color(184, 152, 54, 133),
            new Color(145, 119, 41), new Color(163, 134, 46, 115),
            new Color(172, 147, 49), new Color(179, 153, 51, 128)
    ),
    DEEP_RED(
            new Color(220, 68, 68), new Color(193, 58, 58, 144),
            new Color(193, 71, 71), new Color(184, 54, 54, 133),
            new Color(145, 41, 41), new Color(163, 46, 46, 115),
            new Color(172, 49, 49), new Color(179, 51, 51, 128)
    ),
    LIGHT_BLUE_TRANSPARENT_CENTER(
            new Color(105, 181, 228), new Color(87, 142, 189, 71),
            new Color(99, 156, 191), new Color(92, 140, 181, 65),
            new Color(75, 121, 147), new Color(74, 121, 159, 60),
            new Color(81, 135, 166), new Color(88, 137, 175, 61)
    ),
    LIGHT_BLUE_OPAQUE_CENTER(
            new Color(105, 181, 228), new Color(27, 51, 66, 239),
            new Color(99, 156, 191), new Color(25, 43, 55, 239),
            new Color(75, 121, 147), new Color(20, 34, 44, 239),
            new Color(81, 135, 166), new Color(19, 32, 43, 239)
    ),
    LIGHT_BLUE_OPAQUE_CENTER_LIGHT(
            new Color(105, 181, 228), new Color(35, 66, 87, 239),
            new Color(99, 156, 191), new Color(35, 59, 78, 239),
            new Color(75, 121, 147), new Color(31, 52, 67, 239),
            new Color(81, 135, 166), new Color(29, 48, 66, 239)
    ),
    LIGHT_BLUE_FULLY_OPAQUE_CENTER(
            new Color(105, 181, 228), new Color(27, 51, 66),
            new Color(99, 156, 191), new Color(25, 43, 55),
            new Color(75, 121, 147), new Color(20, 34, 44),
            new Color(81, 135, 166), new Color(19, 32, 43)
    ),
    RED(
            new Color(228, 105, 105), new Color(189, 87, 87, 144),
            new Color(191, 99, 99), new Color(181, 92, 92, 133),
            new Color(147, 75, 75), new Color(159, 74, 74, 115),
            new Color(166, 81, 81), new Color(175, 88, 88, 128)
    ),
    GREEN(
            new Color(121, 228, 105), new Color(101, 189, 87, 144),
            new Color(122, 191, 99), new Color(114, 181, 92, 133),
            new Color(99, 147, 75), new Color(92, 159, 74, 115),
            new Color(97, 166, 81), new Color(107, 175, 88, 128)
    ),
    GREEN_TRANSPARENT_CENTER(
            new Color(121, 228, 105), new Color(101, 189, 87, 71),
            new Color(122, 191, 99), new Color(114, 181, 92, 65),
            new Color(99, 147, 75), new Color(92, 159, 74, 60),
            new Color(97, 166, 81), new Color(107, 175, 88, 61)
    ),
    BLUE(
            new Color(85, 121, 211), new Color(87, 111, 189, 144),
            new Color(92, 119, 186), new Color(92, 117, 181, 133),
            new Color(75, 95, 147), new Color(74, 94, 159, 115),
            new Color(81, 108, 166), new Color(88, 116, 175, 128)
    ),
    YELLOW(
            new Color(228, 208, 105), new Color(189, 169, 87, 144),
            new Color(191, 173, 99), new Color(181, 166, 92, 133),
            new Color(147, 137, 75), new Color(159, 145, 74, 115),
            new Color(166, 149, 81), new Color(175, 158, 88, 128)
    ),
    RED_UNAVAILABLE(
            new Color(135, 87, 87), new Color(145, 92, 92, 144),
            new Color(135, 87, 87), new Color(145, 92, 92, 144),
            new Color(135, 87, 87), new Color(145, 92, 92, 144),
            new Color(135, 87, 87), new Color(145, 92, 92, 144)
    ),
    GREEN_UNAVAILABLE(
            new Color(114, 152, 111), new Color(107, 145, 93, 144),
            new Color(114, 152, 111), new Color(107, 145, 93, 144),
            new Color(114, 152, 111), new Color(107, 145, 93, 144),
            new Color(114, 152, 111), new Color(107, 145, 93, 144)
    ),
    BLUE_UNAVAILABLE(
            new Color(87, 95, 135), new Color(92, 99, 145, 144),
            new Color(87, 95, 135), new Color(92, 99, 145, 144),
            new Color(87, 95, 135), new Color(92, 99, 145, 144),
            new Color(87, 95, 135), new Color(92, 99, 145, 144)
    ),
    YELLOW_UNAVAILABLE(
            new Color(161, 153, 98), new Color(163, 149, 100, 144),
            new Color(161, 153, 98), new Color(163, 149, 100, 144),
            new Color(161, 153, 98), new Color(163, 149, 100, 144),
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

    UIColourTheme(Color borderColour, Color backgroundColour, Color borderColourHover, Color backgroundColourHover,
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
}
