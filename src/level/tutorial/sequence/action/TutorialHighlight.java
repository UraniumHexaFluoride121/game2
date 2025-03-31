package level.tutorial.sequence.action;

import level.Level;
import render.level.tile.HexagonBorder;
import render.level.tile.HighlightTileRenderer;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

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
    public static TutorialHighlight radius(Level l, Color c, int x, int y, int r) {
        HashSet<Point> tiles = l.tileSelector.tilesInRadius(new Point(x, y), r);
        return new TutorialHighlight(l, tiles, c);
    }

    public static TutorialAction disable(Level l) {
        return new TutorialAction(() -> {
            l.levelRenderer.tutorialHighlightRenderer.close();
            l.levelRenderer.tutorialBorderRenderer = null;
        });
    }

    public static TutorialHighlight visibleTiles(Level l, Color c) {
        return new TutorialHighlight(l, () -> l.currentVisibility.visibleTiles(), c);
    }

    public static TutorialHighlight fowTiles(Level l, Color c) {
        return new TutorialHighlight(l, () -> l.tileSelector.tileSet.stream()
                .map(t -> t.pos)
                .filter(p -> !l.currentVisibility.visibleTiles().contains(p))
                .collect(HashSet::new, HashSet::add, HashSet::addAll), c);
    }

    private TutorialHighlight(Level l, HashSet<Point> tiles, Color c) {
        super(() -> {
            Color middle = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
            l.levelRenderer.tutorialHighlightRenderer = new HighlightTileRenderer(middle, tiles, l);
            l.levelRenderer.tutorialBorderRenderer = new HexagonBorder(tiles, c);
        });
    }

    private TutorialHighlight(Level l, Supplier<HashSet<Point>> tiles, Color c) {
        super(() -> {
            HashSet<Point> points = tiles.get();
            Color middle = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
            l.levelRenderer.tutorialHighlightRenderer = new HighlightTileRenderer(middle, points, l);
            l.levelRenderer.tutorialBorderRenderer = new HexagonBorder(points, c);
        });
    }
}
