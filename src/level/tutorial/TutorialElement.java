package level.tutorial;

import level.Level;

import java.util.function.Consumer;

public enum TutorialElement {
    TILE_SELECTION(
            empty(),
            empty()
    ),
    CAMERA_MOVEMENT(
            l -> l.levelRenderer.moveCameraEnd(),
            empty()
    ),
    ACTIONS(
            empty(),
            empty()
    ),
    ACTION_TILE_SELECTION(
            empty(),
            empty()
    ),
    ACTION_DESELECT(
            l -> l.levelRenderer.exitActionButton.setClickEnabled(false),
            l -> l.levelRenderer.exitActionButton.setClickEnabled(true)
    ),
    END_TURN(
            empty(),
            empty()
    ),
    VIEW_FIRING_RANGE(
            l -> l.levelRenderer.uiUnitInfo.viewFiringRange.deselect(),
            empty()
    );

    public final Consumer<Level> onDisable, onEnable;

    TutorialElement(Consumer<Level> onDisable, Consumer<Level> onEnable) {
        this.onDisable = onDisable;
        this.onEnable = onEnable;
    }

    private static Consumer<Level> empty() {
        return l -> {
        };
    }
}
