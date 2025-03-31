package level.tutorial.sequence.action;

import level.Level;
import render.level.tile.HexagonBorder;
import render.level.tile.HighlightTileRenderer;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

public class TutorialHighlight extends TutorialAction {
    public static TutorialHighlight tile(Level l, Color c, int x, int y) {
        HashSet<Point> tiles = new HashSet<>();
        tiles.add(new Point(x, y));
        return new TutorialHighlight(l, tiles, c);
    }

    public static TutorialHighlight tiles(Level l, Color c, Point... points) {
        HashSet<Point> tiles = new HashSet<>(List.of(points));
        return new TutorialHighlight(l, tiles, c);
    }

    public static TutorialAction disable(Level l) {
        return new TutorialAction(() -> {
            l.levelRenderer.tutorialHighlightRenderer.close();
            l.levelRenderer.tutorialBorderRenderer = null;
        });
    }

    private TutorialHighlight(Level l, HashSet<Point> tiles, Color c) {
        super(() -> {
            Color middle = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
            l.levelRenderer.tutorialHighlightRenderer = new HighlightTileRenderer(middle, tiles, l);
            l.levelRenderer.tutorialBorderRenderer = new HexagonBorder(tiles, c);
        });
    }
}
