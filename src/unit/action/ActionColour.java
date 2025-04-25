package unit.action;

import java.awt.*;

public enum ActionColour {
    DISABLED(new Color(101, 101, 101), new Color(156, 156, 156)),
    PINK(new Color(191, 49, 213), new Color(177, 118, 184)), //Warp
    PURPLE(new Color(134, 49, 213), new Color(152, 118, 184)), //Mine
    BLUE(new Color(65, 106, 205), new Color(94, 148, 191)), //Move
    LIGHT_BLUE(new Color(33, 122, 179), new Color(94, 163, 184)), //Regen shield
    LIGHT_BLUE_UNUSABLE(new Color(55, 112, 143), new Color(85, 125, 138)),
    DARK_GREEN(new Color(7, 131, 38), new Color(108, 154, 107)), //Capture
    GREEN(new Color(23, 166, 28), new Color(134, 179, 131)), //Repair
    GREEN_UNUSABLE(new Color(18, 128, 21), new Color(105, 140, 102)), //Repair
    YELLOW(new Color(211, 179, 37), new Color(165, 154, 113)), //Stealth
    YELLOW_UNUSABLE(new Color(149, 131, 59), new Color(129, 121, 89)),
    ORANGE(new Color(213, 131, 49), new Color(184, 147, 118)),
    BROWN(new Color(161, 101, 37), new Color(154, 126, 98)), //Resupply
    BROWN_UNUSABLE(new Color(128, 82, 36), new Color(128, 108, 82)),
    RED(new Color(197, 62, 62), new Color(186, 110, 110)), //Fire
    RED_UNUSABLE(new Color(165, 86, 86), new Color(129, 91, 91));

    public final Color border, background;

    ActionColour(Color border, Color background) {
        this.border = border;
        this.background = background;
    }
}
