package level.tutorial.sequence.action;

import level.tutorial.TutorialManager;
import unit.action.Action;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

public class AllowedActionTiles extends TutorialAction {
    public static AllowedActionTiles enable(Action a, Point... points) {
        return new AllowedActionTiles(() -> TutorialManager.actionTiles.get(a).addAll(new HashSet<>(List.of(points))));
    }

    public static AllowedActionTiles disable(Action a, Point... points) {
        return new AllowedActionTiles(() -> TutorialManager.actionTiles.get(a).removeAll(new HashSet<>(List.of(points))));
    }

    public static AllowedActionTiles only(Action a, Point... points) {
        return new AllowedActionTiles(() -> {
            TutorialManager.actionTiles.get(a).clear();
            TutorialManager.actionTiles.get(a).addAll(new HashSet<>(List.of(points)));
        });
    }

    public static AllowedActionTiles only(Action a, int x, int y) {
        return only(a, new Point(x, y));
    }

    public static AllowedActionTiles all(Action a) {
        return new AllowedActionTiles(TutorialManager.actionTiles.get(a)::clear);
    }

    public static AllowedActionTiles all() {
        return new AllowedActionTiles(TutorialManager::clearActionTiles);
    }

    private AllowedActionTiles(Runnable action) {
        super(action);
    }
}
