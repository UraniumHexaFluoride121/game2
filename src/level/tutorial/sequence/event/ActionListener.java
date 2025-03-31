package level.tutorial.sequence.event;

import unit.action.Action;

public abstract class ActionListener {
    public static TutorialEventListener select(Action action) {
        return e -> e instanceof EventActionSelect a && a.action == action;
    }

    public static TutorialEventListener select() {
        return e -> e instanceof EventActionSelect;
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
