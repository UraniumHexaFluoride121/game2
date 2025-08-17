package unit.stats;

import level.tile.TileModifier;
import level.tile.TileSet;
import render.level.tile.TilePath;
import unit.Unit;
import unit.UnitData;
import unit.bot.VisibilityData;

import java.awt.*;

public class UnitStatManager extends StatManager<Unit> {
    public UnitStatManager() {
        super();
    }

    @Override
    public boolean canAffordMove(TilePath path) {
        return u.getLevel().levelRenderer.energyManager.canAfford(u.data.team, path.getEnergyCost(this, u.getLevel()), false);
    }

    public TileSet getResupplyTiles(Point pos) {
        return TileSet.tilesInRadius(pos, 1, 1, u.getLevel())
                .m(u.getLevel(), t -> t.unitFilter(TileModifier.hasAlliedUnit(u.data.team, u.getLevel()).and(u -> u.stats.consumesAmmo() && u.data.ammo < u.stats.ammoCapacity())));
    }

    public TileSet getRepairTiles(Point pos) {
        return TileSet.tilesInRadius(pos, 1, 1, u.getLevel())
                .m(u.getLevel(), t -> t.unitFilter(TileModifier.hasAlliedUnit(u.data.team, u.getLevel()).and(u2 -> u2.data.renderHP < u2.data.type.hitPoints && u2 != u)));
    }

    public TileSet tilesInMoveRange(VisibilityData visibility) {
        return TileSet.all(u.getLevel()).m(u.getLevel(), t -> t
                .unitFilter(TileModifier.withoutVisibleEnemies(u.data.team, u.getLevel(), visibility))
                .tilesInRange(u.data.pos, this::moveCost, maxMovement())
        );
    }

    @Override
    public TileSet tilesInFiringRange(UnitData data) {
        TileSet tiles = TileSet.tilesInRadius(data.pos, data.type.maxRange, u.getLevel());
        if (data.type.minRange != data.type.maxRange)
            tiles.removeAll(TileSet.tilesInRadius(data.pos, data.type.minRange - 1, u.getLevel()));
        return tiles;
    }

    public TileSet tilesInFiringRange(VisibilityData visibility, UnitData data, boolean onlyWithEnemies) {
        TileSet tiles = new TileSet(u.getLevel());
        if (onlyWithEnemies) {
            tiles.addAll(tilesInFiringRange(data).m(u.getLevel(), t -> t.unitFilter(TileModifier.withEnemiesThatCanBeFiredAt(u, visibility))));
        } else
            tiles.addAll(tilesInFiringRange(data));
        return tiles;
    }
}
