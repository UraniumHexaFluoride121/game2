package unit.stats;

import foundation.Deletable;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tile.TileType;
import render.level.tile.TilePath;
import render.types.text.TextRenderable;
import unit.Unit;
import unit.UnitData;
import unit.action.Action;
import unit.bot.VisibilityData;
import unit.stats.modifiers.MovementModifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static unit.action.Action.*;
import static unit.action.Action.CAPTURE;
import static unit.action.Action.MINE;

public class StatManager implements Deletable {
    public Unit u;

    public StatManager(Unit u) {
        this.u = u;
    }

    public float moveCost(TileType tile) {
        ArrayList<Modifier> modifiers = getMovementModifiers();
        if (tile == TileType.ASTEROIDS && modifiers.contains(MovementModifier.NO_ASTEROID_FIELDS))
            return 100;
        return tile.moveCost * Modifier.multiplicativeEffect(ModifierCategory.MOVEMENT_COST_ALL, modifiers) *
                Modifier.multiplicativeEffect(switch (tile) {
                    case EMPTY -> ModifierCategory.MOVEMENT_COST_EMPTY;
                    case NEBULA -> ModifierCategory.MOVEMENT_COST_NEBULA;
                    case DENSE_NEBULA -> ModifierCategory.MOVEMENT_COST_DENSE_NEBULA;
                    case ASTEROIDS -> ModifierCategory.MOVEMENT_COST_ASTEROIDS;
                }, modifiers);
    }

    public float movementCostMultiplier() {
        return u.type.movementCostMultiplier();
    }

    public int moveEnergyCost(float moveCost) {
        return (int) Math.ceil(moveCost * movementCostMultiplier());
    }

    public float maxMovement() {
        return u.type.maxMovement;
    }

    public ArrayList<Modifier> getMovementModifiers() {
        ArrayList<Modifier> list = new ArrayList<>(u.type.modifiers);
        return list;
    }

    public boolean canAffordMove(TilePath path) {
        return u.getLevel().levelRenderer.energyManager.canAfford(u.team, path.getEnergyCost(this, u.getLevel()), false);
    }

    public float viewRange(TileType tile) {
        return tile.concealment;
    }

    public float maxViewRange() {
        return u.type.maxViewRange;
    }

    public float repair() {
        return u.type.repair;
    }

    public float shieldRegen() {
        return u.type.shieldRegen;
    }

    public float baseShieldRegen() {
        return u.type.shieldRegen;
    }

    public float maxShieldHP() {
        return u.type.shieldHP;
    }

    public float maxHP() {
        return u.type.hitPoints;
    }

    public float baseDamage() {
        return u.type.damage;
    }

    public int ammoCapacity() {
        return u.type.ammoCapacity;
    }

    public boolean consumesAmmo() {
        return ammoCapacity() != 0;
    }

    public TileSet getResupplyTiles(Point pos) {
        return TileSet.tilesInRadius(pos, 1, 1, u.getLevel())
                .m(u.getLevel(), t -> t.unitFilter(TileModifier.hasAlliedUnit(u.team, u.getLevel()).and(u -> u.stats.consumesAmmo() && u.ammo < u.stats.ammoCapacity())));
    }

    public TileSet getRepairTiles(Point pos) {
        return TileSet.tilesInRadius(pos, 1, 1, u.getLevel())
                .m(u.getLevel(), t -> t.unitFilter(TileModifier.hasAlliedUnit(u.team, u.getLevel()).and(u2 -> u2.hitPoints < u2.type.hitPoints && u2 != u)));
    }

    public TileSet tilesInMoveRange(VisibilityData visibility) {
        return TileSet.all(u.getLevel()).m(u.getLevel(), t -> t
                .unitFilter(TileModifier.withoutVisibleEnemies(u.team, u.getLevel(), visibility))
                .tilesInRange(u.pos, this::moveCost, maxMovement())
        );
    }

    public TileSet tilesInFiringRange(UnitData data) {
        TileSet tiles = TileSet.tilesInRadius(data.pos, u.type.maxRange, u.getLevel());
        if (u.type.minRange != u.type.maxRange)
            tiles.removeAll(TileSet.tilesInRadius(data.pos, u.type.minRange - 1, u.getLevel()));
        return tiles;
    }

    public TileSet tilesInFiringRange(VisibilityData visibility, UnitData data, boolean onlyWithEnemies) {
        TileSet tiles = new TileSet(u.getLevel().tilesX, u.getLevel().tilesY);
        if (onlyWithEnemies) {
            tiles.addAll(tilesInFiringRange(data).m(u.getLevel(), t -> t.unitFilter(TileModifier.withEnemiesThatCanBeFiredAt(u, visibility))));
        } else
            tiles.addAll(tilesInFiringRange(data));
        return tiles;
    }

    public boolean hasNonStandardRange() {
        return u.type.maxRange != 1;
    }

    public String getRangeText() {
        if (u.type.minRange != u.type.maxRange)
            return u.type.minRange + " - " + u.type.maxRange + TextRenderable.RANGE_ICON.display;
        else
            return u.type.maxRange + TextRenderable.RANGE_ICON.display;
    }

    public Optional<Integer> getActionCost(Action action) {
        return removeActionEnergyCost(action) ? Optional.empty() : u.type.getActionCost(action);
    }

    public Optional<Integer> getPerTurnActionCost(Action action) {
        return u.type.getPerTurnActionCost(action);
    }

    //Used for toggle actions. If true, one-time action costs are removed, and per-turn action costs are reversed
    public boolean removeActionEnergyCost(Action a) {
        return a == STEALTH && u.stealthMode || a == MINE && u.mining || a == CAPTURE && u.isCapturing();
    }

    @Override
    public void delete() {
        u = null;
    }
}
