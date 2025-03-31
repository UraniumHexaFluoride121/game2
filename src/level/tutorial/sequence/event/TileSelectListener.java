package level.tutorial.sequence.event;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

public class TileSelectListener implements TutorialEventListener {
    private final HashSet<Point> acceptedTiles;

    public static TileSelectListener tile(int x, int y) {
        return tiles(new Point(x, y));
    }

    public static TileSelectListener tiles(Point... points) {
        return new TileSelectListener(new HashSet<>(List.of(points)));
    }

    public static TutorialEventListener any() {
        return e -> e instanceof EventTileSelect;
    }

    private TileSelectListener(HashSet<Point> acceptedTiles) {
        this.acceptedTiles = acceptedTiles;
    }

    @Override
    public boolean test(TutorialEvent tutorialEvent) {
        if (tutorialEvent instanceof EventTileSelect e)
            return acceptedTiles.contains(e.tile);
        return false;
    }
}
