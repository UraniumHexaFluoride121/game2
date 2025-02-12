package unit.action;

import java.awt.*;

public enum ActionColour {
    DISABLED(new Color(101, 101, 101), new Color(156, 156, 156)),
    PURPLE(new Color(134, 49, 213), new Color(152, 118, 184)),
    BLUE(new Color(65, 106, 205), new Color(94, 148, 191)),
    LIGHT_BLUE(new Color(33, 122, 179), new Color(94, 163, 184)),
    DARK_GREEN(new Color(7, 131, 38), new Color(108, 154, 107)),
    GREEN(new Color(30, 174, 63), new Color(122, 188, 119)),
    YELLOW(new Color(213, 186, 49), new Color(177, 166, 120)),
    ORANGE(new Color(213, 131, 49), new Color(184, 147, 118)),
    RED(new Color(197, 62, 62), new Color(186, 110, 110));

    public final Color border, background;

    ActionColour(Color border, Color background) {
        this.border = border;
        this.background = background;
    }
}
