package level.tutorial.sequence.action;

import level.Level;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;

import java.util.HashSet;
import java.util.List;

public class ModifyElements extends TutorialAction {
    public static ModifyElements enable(Level l, TutorialElement... elements) {
        return new ModifyElements(l, true, elements);
    }

    public static ModifyElements disable(Level l, TutorialElement... elements) {
        return new ModifyElements(l, false, elements);
    }

    public static ModifyElements disableAll(Level l) {
        return new ModifyElements(l, false, TutorialElement.values());
    }

    public static ModifyElements actionCameraNoDeselect(Level l) {
        return new ModifyElements(l,
                TutorialElement.TILE_SELECTION,
                TutorialElement.TILE_DESELECTION,
                TutorialElement.ACTIONS,
                TutorialElement.ACTION_TILE_SELECTION,
                TutorialElement.CAMERA_MOVEMENT
        );
    }

    public static ModifyElements actionCamera(Level l) {
        return new ModifyElements(l,
                TutorialElement.TILE_SELECTION,
                TutorialElement.TILE_DESELECTION,
                TutorialElement.ACTIONS,
                TutorialElement.ACTION_TILE_SELECTION,
                TutorialElement.CAMERA_MOVEMENT,
                TutorialElement.ACTION_DESELECT
        );
    }

    public static ModifyElements endTurn(Level l) {
        return new ModifyElements(l,
                TutorialElement.END_TURN
        );
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

    private ModifyElements(Level l, TutorialElement... enabledElements) {
        super(() -> {
            HashSet<TutorialElement> elements = new HashSet<>(List.of(enabledElements));
            for (TutorialElement element : TutorialElement.values()) {
                if (elements.contains(element))
                    TutorialManager.enableElement(l, element);
                else
                    TutorialManager.disableElement(l, element);
            }
        });
    }
}
