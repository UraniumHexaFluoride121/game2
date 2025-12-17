package level.tutorial;

import level.Level;
import unit.UnitTeam;

import java.util.function.Consumer;

import static level.energy.EnergyManager.*;

public enum TutorialElement {
    TUTORIAL_INCOME_DISABLED(
            l -> {
                l.levelRenderer.energyManager.recalculateIncome();
                l.levelRenderer.energyManager.addAvailable(UnitTeam.BLUE, TUTORIAL_INCOME);
            },
            l -> l.levelRenderer.energyManager.recalculateIncome()
    ),
    TILE_SELECTION(
            empty(),
            empty()
    ),
    TILE_DESELECTION(
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
    ),
    VIEW_EFFECTIVENESS(
            l -> l.levelRenderer.uiUnitInfo.viewEffectiveness.deselect(),
            empty()
    ),
    VIEW_FIRING_RANGE_DESELECT(
            empty(),
            empty()
    ),
    VIEW_EFFECTIVENESS_DESELECT(
            empty(),
            empty()
    ),
    BOT(
            empty(),
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
