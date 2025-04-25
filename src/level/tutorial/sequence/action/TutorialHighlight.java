package level.tutorial.sequence.action;

import level.Level;
import level.tile.TileSet;
import render.level.tile.HexagonBorder;
import render.level.tile.HighlightTileRenderer;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

public class TutorialHighlight extends TutorialAction {
    public static TutorialHighlight tile(Level l, Color c, int x, int y) {
        TileSet tiles = new TileSet(l.tilesX, l.tilesY);
        tiles.add(new Point(x, y));
        return new TutorialHighlight(l, tiles, c);
    }

    public static TutorialHighlight tiles(Level l, Color c, Point... points) {
        TileSet tiles = new TileSet(l.tilesX, l.tilesY, List.of(points));
        return new TutorialHighlight(l, tiles, c);
    }
    public static TutorialHighlight radius(Level l, Color c, int x, int y, int r) {
        TileSet tiles = TileSet.tilesInRadius(new Point(x, y), r, l);
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
                .collect(() -> new TileSet(l.tilesX, l.tilesY), TileSet::add, TileSet::addAll), c);
    }

    private TutorialHighlight(Level l, TileSet tiles, Color c) {
        super(() -> {
            Color middle = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
            l.levelRenderer.tutorialHighlightRenderer = new HighlightTileRenderer(middle, tiles, l);
            l.levelRenderer.tutorialBorderRenderer = new HexagonBorder(tiles, c);
        });
    }

    private TutorialHighlight(Level l, Supplier<TileSet> tiles, Color c) {
        super(() -> {
            TileSet points = tiles.get();
            Color middle = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
            l.levelRenderer.tutorialHighlightRenderer = new HighlightTileRenderer(middle, points, l);
            l.levelRenderer.tutorialBorderRenderer = new HexagonBorder(points, c);
        });
    }
}
