package level.tutorial.sequence.action;

import level.Level;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import unit.action.Action;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static level.tutorial.TutorialManager.*;

public class ModifyElements extends TutorialAction {
    private final TutorialElement[] elements;
    private Level l;

    public static ModifyElements enable(Level l, TutorialElement... elements) {
        return new ModifyElements(l, true, false, elements);
    }

    public static ModifyElements permanentlyEnable(Level l, TutorialElement... elements) {
        return new ModifyElements(l, true, true, elements);
    }

    public static ModifyElements disable(Level l, TutorialElement... elements) {
        return new ModifyElements(l, false, false, elements);
    }

    public static ModifyElements disableAll(Level l) {
        return new ModifyElements(l, false, false, TutorialElement.values());
    }

    public static ModifyElements enableAll(Level l) {
        return new ModifyElements(l, true, false, TutorialElement.values());
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

    public static ModifyElements action(Level l, boolean camera) {
        return new ModifyElements(l,
                TutorialElement.TILE_SELECTION,
                TutorialElement.TILE_DESELECTION,
                TutorialElement.ACTIONS,
                TutorialElement.ACTION_TILE_SELECTION,
                camera ? TutorialElement.CAMERA_MOVEMENT : TutorialElement.TILE_SELECTION,
                TutorialElement.ACTION_DESELECT
        );
    }

    public static ModifyElements viewFiringRange(Level l, boolean disable) {
        return new ModifyElements(l,
                TutorialElement.ACTION_DESELECT,
                TutorialElement.VIEW_FIRING_RANGE,
                disable ? TutorialElement.VIEW_FIRING_RANGE_DESELECT : TutorialElement.ACTION_DESELECT
        );
    }

    public static ModifyElements viewEffectiveness(Level l, boolean disable) {
        return new ModifyElements(l,
                TutorialElement.ACTION_DESELECT,
                TutorialElement.VIEW_EFFECTIVENESS,
                disable ? TutorialElement.VIEW_EFFECTIVENESS_DESELECT : TutorialElement.ACTION_DESELECT
        );
    }

    public static ModifyElements tileSelect(Level l, boolean camera) {
        return new ModifyElements(l,
                TutorialElement.TILE_SELECTION,
                TutorialElement.TILE_DESELECTION,
                camera ? TutorialElement.CAMERA_MOVEMENT : TutorialElement.TILE_SELECTION
        );
    }

    public static SubSequence tileSelect(Level l, boolean camera, int x, int y) {
        return SubSequence.sequence(
                tileSelect(l, camera),
                AllowedTiles.only(x, y),
                TutorialHighlight.tile(l, BLUE_HIGHLIGHT, x, y)
        );
    }

    public static SubSequence attack(Level l, boolean camera, int fromX, int fromY) {
        return SubSequence.sequence(
                action(l, camera),
                AllowedActions.only(Action.FIRE),
                AllowedActionTiles.all(),
                AllowedTiles.only(fromX, fromY)
        );
    }

    public static SubSequence tileSelect(Level l, boolean camera, Point... points) {
        return SubSequence.sequence(
                tileSelect(l, camera),
                AllowedTiles.only(points),
                TutorialHighlight.tiles(l, BLUE_HIGHLIGHT, points)
        );
    }

    public static ModifyElements forceTileSelect(Level l, int x, int y) {
        return new ModifyElements(l, l2 -> {
            Point p = new Point(x, y);
            boolean remove = !selectableTiles.contains(p);
            selectableTiles.add(p);
            l2.tileSelector.select(l2.getTile(x, y));
            if (remove)
                selectableTiles.remove(p);
        }, TutorialElement.TILE_SELECTION);
    }

    public static SubSequence moveUnit(Level l, boolean camera, int fromX, int fromY, int toX, int toY) {
        return SubSequence.sequence(
                AllowedTiles.only(fromX, fromY),
                AllowedActionTiles.only(Action.MOVE, toX, toY),
                AllowedActions.only(Action.MOVE),
                TutorialHighlight.tile(l, GREEN_HIGHLIGHT, toX, toY),
                action(l, camera)
        );
    }

    public static SubSequence moveUnit(Level l, boolean camera, Point[] from, Point[] to) {
        return SubSequence.sequence(
                AllowedTiles.only(from),
                AllowedActionTiles.only(Action.MOVE, to),
                AllowedActions.only(Action.MOVE),
                TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, to),
                ModifyElements.action(l, camera)
        );
    }

    public static ModifyElements endTurn(Level l) {
        return new ModifyElements(l,
                TutorialElement.END_TURN
        );
    }

    public ModifyElements add(boolean permanent) {
        return new ModifyElements(l, true, permanent, elements);
    }

    public ModifyElements remove(boolean permanent) {
        return new ModifyElements(l, false, permanent, elements);
    }

    private ModifyElements(Level l, boolean enable, boolean permanent, TutorialElement... elements) {
        super(() -> {
            for (TutorialElement element : elements) {
                if (enable)
                    if (permanent)
                        TutorialManager.permanentlyEnableElement(l, element);
                    else
                        TutorialManager.enableElement(l, element);
                else
                    TutorialManager.disableElement(l, element);
            }
        });
        this.elements = elements;
        this.l = l;
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
        elements = enabledElements;
        this.l = l;
    }

    private ModifyElements(Level l, Consumer<Level> action, TutorialElement... enabledElements) {
        super(() -> {
            HashSet<TutorialElement> elements = new HashSet<>(List.of(enabledElements));
            elements.removeIf(TutorialManager::isEnabled);
            for (TutorialElement element : elements) {
                TutorialManager.enableElement(l, element);
            }
            action.accept(l);
            for (TutorialElement element : elements) {
                TutorialManager.disableElement(l, element);
            }
        });
        elements = enabledElements;
        this.l = l;
    }

    @Override
    public void delete() {
        super.delete();
        l = null;
    }
}
