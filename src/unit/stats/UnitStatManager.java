package unit.stats;

import foundation.math.MathUtil;
import level.tile.TileModifier;
import level.tile.TileSet;
import render.level.tile.TilePath;
import unit.Unit;
import unit.UnitData;
import unit.action.Action;
import unit.bot.VisibilityData;
import unit.stats.attribute.UnitAttribute;
import unit.stats.modifiers.groups.CardModifiers;
import unit.stats.modifiers.types.Modifier;
import unit.stats.modifiers.types.ModifierCategory;
import unit.stats.modifiers.types.SingleModifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class UnitStatManager extends StatManager<Unit> {
    public UnitStatManager() {
        super();
    }

    @Override
    public ArrayList<Modifier> getMovementModifiers() {
        ArrayList<Modifier> modifiers = super.getMovementModifiers();
        float cardModifier = u.fromTeamData(d -> d.getCardUnitModifier(ModifierCategory.MOVEMENT_COST_ALL, u, Float::sum));
        if (!MathUtil.equal(cardModifier, 1, 0.001f))
            modifiers.add(new SingleModifier(cardModifier, "Card Modifiers", "The combined " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " modifier from all applicable cards.",
                    Modifier::percentMultiplicative, ModifierCategory.MOVEMENT_COST_ALL)
                    .setListAndMainColour(Modifier.listColourFromEffectPercentMultiplicative(cardModifier, false))
            );
        return modifiers;
    }

    @Override
    public float attackDamage() {
        return super.attackDamage() * u.fromTeamData(d -> d.getCardUnitModifier(ModifierCategory.DAMAGE, u, Float::sum));
    }

    @Override
    public float maxHP() {
        return super.maxHP() + u.fromTeamData(d -> Math.round(d.getCardUnitModifier(ModifierCategory.HP, u, Float::sum)));
    }

    @Override
    public int ammoCapacity() {
        return super.ammoCapacity() + u.fromTeamData(d -> Math.round(d.getCardUnitModifier(ModifierCategory.AMMO_CAPACITY, u, Float::sum)));
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

    @Override
    public Optional<Integer> getActionCost(Action action) {
        return super.getActionCost(action)
                .map(c -> u.fromTeamData(d ->
                        Math.max(
                                (int) Math.round(Math.ceil(c * CardModifiers.MAX_ACTION_COST_REDUCTION)),
                                Math.round(c + d.getCardActionModifier(ModifierCategory.ACTION_COST, u, action, Float::sum)))
                        )
                );
    }

    @Override
    public Optional<Integer> getPerTurnActionCost(Action action) {
        return super.getPerTurnActionCost(action)
                .map(c -> u.fromTeamData(d ->
                                Math.max(
                                        (int) Math.round(Math.ceil(c * CardModifiers.MAX_ACTION_COST_REDUCTION)),
                                        Math.round(c + d.getCardActionModifier(ModifierCategory.ACTION_COST_PER_TURN, u, action, Float::sum)))
                        )
                );
    }

    @Override
    public boolean hasUnitAttribute(UnitAttribute a) {
        return super.hasUnitAttribute(a) || u.fromTeamData(d -> d.getCardUnitModifier(UnitAttribute.getCategory(a), u, Float::max) != 0);
    }
}
