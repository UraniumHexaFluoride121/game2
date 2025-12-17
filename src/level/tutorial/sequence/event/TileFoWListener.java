package level.tutorial.sequence.event;

import foundation.MainPanel;
import level.Level;
import level.tile.Tile;
import level.tutorial.TutorialManager;
import unit.Unit;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class TileFoWListener implements TutorialEventListener {
    private Level level;
    private final BiPredicate<Level, Tile> acceptedTiles;
    private boolean requiresAll = false;

    public static TileFoWListener tile(Level level, int x, int y) {
        return tiles(level, new Point(x, y));
    }

    public static TileFoWListener tiles(Level level, Point... points) {
        return new TileFoWListener(level, new HashSet<>(List.of(points)));
    }

    public static TileFoWListener withUnit(Level level, Predicate<Unit> filter) {
        return new TileFoWListener(level, (l, tile) -> {
            return filter.test(l.getUnit(tile.pos));
        });
    }

    private TileFoWListener(Level level, HashSet<Point> points) {
        this(level, (l, tile) -> points.contains(tile.pos));
    }

    private TileFoWListener(Level level, BiPredicate<Level, Tile> acceptedTiles) {
        this.level = level;
        this.acceptedTiles = acceptedTiles;
    }

    public TileFoWListener setRequiresAll() {
        requiresAll = true;
        return this;
    }

    @Override
    public boolean test(TutorialEvent tutorialEvent) {
        if (tutorialEvent instanceof EventTilesFoW e) {
            if (requiresAll) {
                for (Tile tile : level.tileSelector.tileSet) {
                    if (!tile.isFoW && !acceptedTiles.test(level, tile)) {
                        return false;
                    }
                }
                return true;
            } else {
                for (Tile tile : level.tileSelector.tileSet) {
                    if (!tile.isFoW && acceptedTiles.test(level, tile)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public void init() {
        TutorialEventListener.super.init();
        MainPanel.addTask(() -> TutorialManager.acceptEvent(new EventTilesFoW(level)));
    }

    @Override
    public void delete() {
        TutorialEventListener.super.delete();
        level = null;
    }
}
