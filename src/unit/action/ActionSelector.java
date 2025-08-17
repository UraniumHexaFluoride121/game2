package unit.action;

import foundation.math.ObjPos;
import level.energy.EnergyCostDisplay;
import level.tile.TileType;
import unit.Unit;

import java.util.Optional;
import java.util.function.Supplier;

import static unit.action.Action.*;

public class ActionSelector extends AbstractActionSelector {
    private Unit unit;

    public ActionSelector(Supplier<Boolean> isVisible, Unit unit) {
        super(isVisible);
        this.unit = unit;
    }

    public void setActions(Action[] actions, Unit unit) {
        actionMap.clear();
        for (Action action : actions) {
            addAction(action, unit);
        }
    }

    public void updateActions(Unit unit) {
        if (actionMap.containsKey(FIRE)) {
            actionMap.get(FIRE).type = !unit.stats.tilesInFiringRange(unit.getLevel().currentVisibility, unit.data, true).isEmpty() &&
                    !unit.data.stealthMode && (unit.data.ammo > 0 || !unit.stats.consumesAmmo()) ? ActionIconType.ENABLED : unit.data.hasPerformedAction(FIRE) ? ActionIconType.DISABLED : ActionIconType.UNUSABLE;
        }
        if (actionMap.containsKey(MOVE)) {
            actionMap.get(MOVE).type = ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(STEALTH)) {
            actionMap.get(STEALTH).type = ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(MINE)) {
            actionMap.get(MINE).type = unit.getLevel().getTile(unit.data.pos).type == TileType.ASTEROIDS ? ActionIconType.ENABLED : ActionIconType.UNUSABLE;
        }
        if (actionMap.containsKey(REPAIR)) {
            actionMap.get(REPAIR).type = unit.stats.getRepairTiles(unit.data.pos).isEmpty() ? ActionIconType.UNUSABLE : ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(RESUPPLY)) {
            actionMap.get(RESUPPLY).type = unit.stats.getResupplyTiles(unit.data.pos).isEmpty() ? ActionIconType.UNUSABLE : ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(SHIELD_REGEN)) {
            actionMap.get(SHIELD_REGEN).type = unit.data.shieldRenderHP < unit.stats.maxShieldHP() ? ActionIconType.ENABLED : ActionIconType.UNUSABLE;
        }
        if (unit.canCapture()) {
            addActionEnabled(CAPTURE, unit);
            actionMap.get(CAPTURE).type = !unit.data.hasPerformedAction(CAPTURE) ? ActionIconType.ENABLED : ActionIconType.DISABLED;
        } else {
            removeAction(CAPTURE);
        }
        actionMap.forEach((a, d) -> {
            if (unit.data.hasPerformedAction(a))
                d.type = ActionIconType.DISABLED;
        });
    }

    @Override
    protected ObjPos getRenderPos() {
        return unit.getRenderPos();
    }

    @Override
    protected Optional<Integer> getPerTurnActionCost(Action a) {
        return unit.stats.getPerTurnActionCost(a);
    }

    @Override
    protected Optional<Integer> getActionCost(Action a) {
        return unit.stats.getActionCost(a);
    }

    @Override
    protected boolean removeActionEnergyCost(Action a) {
        return unit.stats.removeActionEnergyCost(a);
    }

    @Override
    protected void renderPerTurnCostDisplay(EnergyCostDisplay display) {
        display.renderToEnergyManager(unit.getLevel());
    }

    @Override
    protected void renderActionCostDisplay(EnergyCostDisplay display) {
        display.renderToEnergyManager(unit.getLevel());
    }

    @Override
    protected void setCost(EnergyCostDisplay display, Action a, int cost) {
        display.setCost(cost, unit.getLevel());
    }

    @Override
    public void delete() {
        super.delete();
        unit = null;
    }
}
