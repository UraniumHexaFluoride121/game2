package level.tutorial.sequence.action;

import level.tutorial.TutorialManager;
import unit.action.Action;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

public class AllowedTiles extends TutorialAction {
    public static AllowedTiles enable(Point... points) {
        return new AllowedTiles(() -> TutorialManager.selectableTiles.addAll(new HashSet<>(List.of(points))));
    }

    public static AllowedTiles disable(Point... points) {
        return new AllowedTiles(() -> TutorialManager.selectableTiles.removeAll(new HashSet<>(List.of(points))));
    }

    public static AllowedTiles only(Point... points) {
        return new AllowedTiles(() -> {
            TutorialManager.selectableTiles.clear();
            TutorialManager.selectableTiles.addAll(new HashSet<>(List.of(points)));
        });
    }

    public static AllowedTiles only(int x, int y) {
        return only(new Point(x, y));
    }

    public static AllowedTiles all() {
        return new AllowedTiles(TutorialManager.selectableTiles::clear);
    }

    private AllowedTiles(Runnable action) {
        super(action);
    }
}
