package level.tutorial.sequence.action;

import level.Level;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;

public class ModifyElements extends TutorialAction {
    public static ModifyElements enable(Level l, TutorialElement... elements) {
        return new ModifyElements(l, true, elements);
    }

    public static ModifyElements disable(Level l, TutorialElement... elements) {
        return new ModifyElements(l, false, elements);
    }

    private ModifyElements(Level l, boolean enable, TutorialElement... elements) {
        super(() -> {
            for (TutorialElement element : elements) {
                if (enable)
                    TutorialManager.enableElement(l, element);
                else
                    TutorialManager.disableElement(l, element);
            }
        });
    }
}
