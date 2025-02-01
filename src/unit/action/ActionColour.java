package unit.action;

import java.awt.*;

public enum ActionColour {
    DISABLED(new Color(101, 101, 101), new Color(156, 156, 156)),
    BLUE(new Color(65, 106, 205), new Color(94, 148, 191)),
    RED(new Color(197, 62, 62), new Color(186, 110, 110));

    public final Color border, background;

    ActionColour(Color border, Color background) {
        this.border = border;
        this.background = background;
    }
}
