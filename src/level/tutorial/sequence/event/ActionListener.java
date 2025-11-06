package level.tutorial.sequence.event;

import unit.action.Action;

import java.awt.*;

public abstract class ActionListener {
    public static TutorialEventListener tileSelect(Action action, int x, int y) {
        return e -> e instanceof EventActionTileSelect a && a.action == action && a.pos.x == x && a.pos.y == y;
    }

    public static TutorialEventListener tileSelectAnyOf(Action action, Point... points) {
        return e -> {
            if (e instanceof EventActionTileSelect a && a.action == action) {
                for (Point point : points) {
                    if (a.pos.x == point.x && a.pos.y == point.y)
                        return true;
                }
                return false;
            }
            return false;
        };
    }

    public static TutorialEventListener tileSelectAnyExcept(Action action, Point... points) {
        return e -> {
            if (e instanceof EventActionTileSelect a && a.action == action) {
                for (Point point : points) {
                    if (a.pos.x == point.x && a.pos.y == point.y)
                        return false;
                }
                return true;
            }
            return false;
        };
    }

    public static TutorialEventListener select(Action action) {
        return e -> e instanceof EventActionSelect a && a.action == action;
    }

    public static TutorialEventListener select() {
        return e -> e instanceof EventActionSelect;
    }

    public static TutorialEventListener deselect() {
        return e -> e instanceof EventActionDeselect;
    }

    public static TutorialEventListener deselect(Action action) {
        return e -> e instanceof EventActionDeselect a && a.action == action;
    }

    public static TutorialEventListener perform(Action action) {
        return e -> e instanceof EventActionPerform a && a.action == action;
    }

    public static TutorialEventListener perform() {
        return e -> e instanceof EventActionPerform;
    }

    public static TutorialEventListener complete(Action action) {
        return e -> e instanceof EventActionComplete a && a.action == action;
    }

    public static TutorialEventListener complete() {
        return e -> e instanceof EventActionComplete;
    }
}
