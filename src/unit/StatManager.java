package unit;

import foundation.Deletable;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tile.TileType;
import unit.action.Action;

import java.awt.*;
import java.util.Optional;

public class StatManager implements Deletable {
    public Unit u;

    public StatManager(Unit u) {
        this.u = u;
    }

    public float moveCost(TileType tile) {
        return u.type.moveCost(tile);
    }

    public float viewRange(TileType tile) {
        return u.type.viewRange(tile);
    }

    public float maxMovement() {
        return u.type.maxMovement;
    }

    public float maxViewRange() {
        return u.type.maxViewRange;
    }

    public float movementCostMultiplier() {
        return u.type.movementCostMultiplier();
    }

    public float movementFixedCost() {
        return u.type.movementFixedCost();
    }

    public float repair() {
        return u.type.repair;
    }

    public float shieldRegen() {
        return u.type.shieldRegen;
    }

    @Override
    public void delete() {
        u = null;
    }

    public TileSet getResupplyTiles(Point pos) {
        return TileSet.tilesInRadius(pos, 1, 1, u.getLevel())
                .m(u.getLevel(), t -> t.unitFilter(TileModifier.hasAlliedUnit(u.team, u.getLevel()).and(u -> u.getAmmoWeapon() != null && u.getAmmoWeapon().ammo < u.getAmmoWeapon().ammoCapacity)));
    }

    public TileSet getRepairTiles(Point pos) {
        return TileSet.tilesInRadius(pos, 1, 1, u.getLevel())
                .m(u.getLevel(), t -> t.unitFilter(TileModifier.hasAlliedUnit(u.team, u.getLevel()).and(u2 -> u2.hitPoints < u2.type.hitPoints && u2 != u)));
    }

    public Optional<Integer> getActionCost(Action action) {
        return u.type.getActionCost(action);
    }

    public Optional<Integer> getPerTurnActionCost(Action action) {
        return u.type.getPerTurnActionCost(action);
    }
}
