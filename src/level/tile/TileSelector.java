package level.tile;

import foundation.input.InputType;
import foundation.math.ObjPos;
import level.Level;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventTileSelect;
import unit.Unit;
import unit.UnitTeam;
import unit.bot.VisibilityData;

import java.awt.*;
import java.util.HashSet;

public class TileSelector extends AbstractTileSelector<Level> {
    public TileSelector(Level level) {
        super(level);
    }

    public HashSet<Point> withEnemyUnits(HashSet<Point> tiles, Unit unit, VisibilityData visibility) {
        HashSet<Point> enemyTiles = new HashSet<>();
        tiles.forEach(t -> {
            Unit u = level.getUnit(t);
            if (u != null && u.visible(visibility) && unit.canFireAt(u))
                enemyTiles.add(t);
        });
        return enemyTiles;
    }

    public HashSet<Point> tilesWithoutEnemies(HashSet<Point> tiles, UnitTeam team) {
        HashSet<Point> newTiles = new HashSet<>();
        for (Point tile : tiles) {
            Unit u = level.getUnit(tile);
            if (u == null || level.samePlayerTeam(u.team, team) || !u.renderVisible())
                newTiles.add(tile);
        }
        return newTiles;
    }

    public HashSet<Point> tilesWithoutEnemies(HashSet<Point> tiles, UnitTeam team, VisibilityData visibility) {
        HashSet<Point> newTiles = new HashSet<>();
        for (Point tile : tiles) {
            Unit u = level.getUnit(tile);
            if (u == null || level.samePlayerTeam(u.team, team) || !u.visible(visibility))
                newTiles.add(tile);
        }
        return newTiles;
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

    public void deselect() {
        super.deselect();
        level.levelRenderer.tileInfo.setEnabled(false);
        level.updateSelectedUnit();
        if (level.levelRenderer.uiUnitInfo.showFiringRange)
            level.levelRenderer.uiUnitInfo.closeFiringRangeView();
    }

    public void select(Tile tile) {
        if (tile == null || TutorialManager.tileNotSelectable(tile.pos))
            return;
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
        return super.allowDoubleClickToMoveCamera() && TutorialManager.isEnabled(TutorialElement.TILE_SELECTION);
    }

    @Override
    protected void escapePressed() {
        if (TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT))
            deselectAction();
    }

    @Override
    protected void mKeyPressed() {
        level.levelRenderer.mapUI.setEnabled(true);
    }

    public void deselectAction() {
        if (level.hasActiveAction()) {
            if (level.levelRenderer.highlightTileRenderer != null)
                level.levelRenderer.highlightTileRenderer.close();
            level.levelRenderer.unitTileBorderRenderer = null;
            level.setActiveAction(null);
        } else
            deselect();
    }
}
