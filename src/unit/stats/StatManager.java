package unit.stats;

import foundation.Deletable;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tile.TileType;
import render.level.tile.TilePath;
import render.types.text.TextRenderable;
import unit.Unit;
import unit.UnitData;
import unit.UnitLike;
import unit.action.Action;
import unit.bot.VisibilityData;
import unit.stats.modifiers.MovementModifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static unit.action.Action.*;

public class StatManager<T extends UnitLike<?>> implements Deletable {
    public T u;

    public StatManager() {
    }

    public void init(T u) {
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
        return u.data.type.movementCostMultiplier();
    }

    public int moveEnergyCost(float moveCost) {
        return (int) Math.ceil(moveCost * movementCostMultiplier());
    }

    public float maxMovement() {
        return u.data.type.maxMovement;
    }

    public ArrayList<Modifier> getMovementModifiers() {
        return new ArrayList<>(u.data.type.modifiers);
    }

    public boolean canAffordMove(TilePath path) {
        return true;
    }

    public float viewRange(TileType tile) {
        return tile.concealment;
    }

    public float maxViewRange() {
        return u.data.type.maxViewRange;
    }

    public float repair() {
        return u.data.type.repair;
    }

    public float shieldRegen() {
        return u.data.type.shieldRegen;
    }

    public float baseShieldRegen() {
        return u.data.type.shieldRegen;
    }

    public float maxShieldHP() {
        return u.data.type.shieldHP;
    }

    public float maxHP() {
        return u.data.type.hitPoints;
    }

    public float baseDamage() {
        return u.data.type.damage;
    }

    public int ammoCapacity() {
        return u.data.type.ammoCapacity;
    }

    public boolean consumesAmmo() {
        return ammoCapacity() != 0;
    }

    public TileSet tilesInFiringRange(UnitData data) {
        TileSet tiles = TileSet.tilesInRadius(data.pos, data.type.maxRange, null);
        if (data.type.minRange != data.type.maxRange)
            tiles.removeAll(TileSet.tilesInRadius(data.pos, data.type.minRange - 1, null));
        return tiles;
    }

    public boolean hasNonStandardRange() {
        return u.data.type.maxRange != 1;
    }

    public String getRangeText() {
        if (u.data.type.minRange != u.data.type.maxRange)
            return u.data.type.minRange + " - " + u.data.type.maxRange + TextRenderable.RANGE_ICON.display;
        else
            return u.data.type.maxRange + TextRenderable.RANGE_ICON.display;
    }

    public Optional<Integer> getActionCost(Action action) {
        return removeActionEnergyCost(action) ? Optional.empty() : u.data.type.getActionCost(action);
    }

    public Optional<Integer> getPerTurnActionCost(Action action) {
        return u.data.type.getPerTurnActionCost(action);
    }

    //Used for toggle actions. If true, one-time action costs are removed, and per-turn action costs are reversed
    public boolean removeActionEnergyCost(Action a) {
        return a == STEALTH && u.data.stealthMode || a == MINE && u.data.mining;
    }

    @Override
    public void delete() {
        u = null;
    }
}
