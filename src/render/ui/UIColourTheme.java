package render.ui;

import java.awt.*;

public enum UIColourTheme {
    LIGHT_BLUE(
            new Color(105, 181, 228),
            new Color(87, 142, 189, 144),
            new Color(99, 156, 191),
            new Color(92, 140, 181, 133),
            new Color(75, 121, 147),
            new Color(74, 121, 159, 115),
            new Color(81, 135, 166),
            new Color(88, 137, 175, 128)
    ),
    GREEN_SELECTED(
            new Color(105, 181, 228),
            new Color(87, 142, 189, 144),
            new Color(99, 170, 191),
            new Color(92, 140, 181, 133),
            new Color(75, 147, 109),
            new Color(74, 159, 108, 115),
            new Color(99, 193, 93),
            new Color(88, 175, 105, 128)
    ),
    GREEN_SELECTED_TAB(
            new Color(92, 166, 211),
            new Color(87, 142, 189, 144),
            new Color(99, 170, 191),
            new Color(92, 140, 181, 133),
            new Color(75, 147, 109),
            new Color(74, 159, 108, 115),
            new Color(99, 193, 93),
            new Color(88, 175, 105, 128)
    ),
    LIGHT_BLUE_TRANSPARENT_CENTER(
            new Color(105, 181, 228),
            new Color(87, 142, 189, 71),
            new Color(99, 156, 191),
            new Color(92, 140, 181, 65),
            new Color(75, 121, 147),
            new Color(74, 121, 159, 60),
            new Color(81, 135, 166),
            new Color(88, 137, 175, 61)
    ),
    RED(
            new Color(228, 105, 105),
            new Color(189, 87, 87, 144),
            new Color(191, 99, 99),
            new Color(181, 92, 92, 133),
            new Color(147, 75, 75),
            new Color(159, 74, 74, 115),
            new Color(166, 81, 81),
            new Color(175, 88, 88, 128)
    ),
    GREEN(
            new Color(121, 228, 105),
            new Color(101, 189, 87, 144),
            new Color(122, 191, 99),
            new Color(114, 181, 92, 133),
            new Color(99, 147, 75),
            new Color(92, 159, 74, 115),
            new Color(97, 166, 81),
            new Color(107, 175, 88, 128)
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
