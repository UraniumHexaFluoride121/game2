package level.tile;

import foundation.input.InputType;
import foundation.math.ObjPos;
import level.Level;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventActionDeselect;
import level.tutorial.sequence.event.EventTileSelect;
import unit.action.Action;

import java.awt.*;

public class TileSelector extends AbstractTileSelector<Level> {
    public TileSelector(Level level) {
        super(level);
    }

    @Override
    public Tile tileAtSelectablePos(ObjPos pos) {
        if (level.hasActiveAction()) {
            for (Point p : level.selectedUnit.selectableTiles()) {
                Tile tile = tiles[p.x][p.y];
                if (tile.posInside(pos)) {
                    return tile;
                }
            }
            return null;
        } else {
            return super.tileAtSelectablePos(pos);
        }
    }

    @Override
    public void deselect() {
        if (TutorialManager.isDisabled(TutorialElement.TILE_DESELECTION))
            return;
        super.deselect();
        level.levelRenderer.tileInfo.setEnabled(false);
        level.updateSelectedUnit();
        level.levelRenderer.uiUnitInfo.closeFiringRangeView();
        level.levelRenderer.uiUnitInfo.closeEffectivenessView();
    }

    @Override
    public void select(Tile tile) {
        if (tile == null) {
            deselect();
            return;
        }
        if (TutorialManager.tileNotSelectable(tile.pos))
            return;
        level.levelRenderer.uiUnitInfo.onTileSelected();
        super.select(tile);
        level.levelRenderer.tileInfo.setTile(tile);
        level.levelRenderer.tileInfo.setEnabled(true);
        level.updateSelectedUnit();
        TutorialManager.acceptEvent(new EventTileSelect(level, tile.pos));
    }

    @Override
    protected void mouseLeft(ObjPos pos, boolean inside, InputType type) {
        if (!level.hasActiveAction()) {
            super.mouseLeft(pos, inside, type);
        } else {
            level.selectedUnit.onTileClicked(tileAtSelectablePos(pos), level.getActiveAction());
        }
        level.updateSelectedUnit();
    }

    @Override
    protected boolean allowDoubleClickToMoveCamera() {
        return super.allowDoubleClickToMoveCamera() && TutorialManager.isEnabled(TutorialElement.TILE_SELECTION) && TutorialManager.isEnabled(TutorialElement.CAMERA_MOVEMENT);
    }

    @Override
    protected void escapePressed() {
        deselectAction();
    }

    @Override
    protected void mKeyPressed() {
        level.levelRenderer.mapUI.setEnabled(true);
    }

    public void deselectAction() {
        if (TutorialManager.isDisabled(TutorialElement.ACTION_DESELECT)) {
            if (!level.hasActiveAction())
                deselect();
            return;
        }
        if (level.hasActiveAction()) {
            Action action = level.getActiveAction();
            if (level.levelRenderer.highlightTileRenderer != null)
                level.levelRenderer.highlightTileRenderer.close();
            level.levelRenderer.unitTileBorderRenderer = null;
            level.setActiveAction(null);
            TutorialManager.acceptEvent(new EventActionDeselect(level, action));
        } else
            deselect();
    }
}
