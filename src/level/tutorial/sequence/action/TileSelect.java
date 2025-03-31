package level.tutorial.sequence.action;

import level.Level;

import java.awt.*;

public class TileSelect extends TutorialAction {
    public static TileSelect tile(Level level, int x, int y) {
        return new TileSelect(level, new Point(x, y));
    }

    public static TutorialAction deselect(Level l) {
        return new TutorialAction(() -> l.tileSelector.deselect());
    }

    private TileSelect(Level l, Point pos) {
        super(() -> l.tileSelector.select(l.getTile(pos)));
    }
}
