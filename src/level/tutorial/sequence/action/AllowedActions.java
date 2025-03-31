package level.tutorial.sequence.action;

import level.tutorial.TutorialManager;
import unit.action.Action;

import java.util.HashSet;
import java.util.List;

public class AllowedActions extends TutorialAction {
    public static AllowedActions enable(Action... actions) {
        return new AllowedActions(() -> TutorialManager.allowedActions.addAll(new HashSet<>(List.of(actions))));
    }

    public static AllowedActions disable(Action... actions) {
        return new AllowedActions(() -> TutorialManager.allowedActions.removeAll(new HashSet<>(List.of(actions))));
    }

    public static AllowedActions only(Action... actions) {
        return new AllowedActions(() -> {
            TutorialManager.allowedActions.clear();
            TutorialManager.allowedActions.addAll(new HashSet<>(List.of(actions)));
        });
    }

    public static AllowedActions all() {
        return new AllowedActions(() -> {
            Action.forEach(TutorialManager.allowedActions::add);
        });
    }

    private AllowedActions(Runnable action) {
        super(action);
    }
}
