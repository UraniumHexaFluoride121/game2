package render.types.box.display.tutorial;

import foundation.math.ObjPos;
import level.energy.EnergyCostDisplay;
import unit.UnitLike;
import unit.action.AbstractActionSelector;
import unit.action.Action;
import unit.action.ActionIconType;

import java.util.Optional;

public class TutorialActionSelector extends AbstractActionSelector {
    private boolean enabled = false;
    private ObjPos renderPos = new ObjPos();
    private UnitLike unit = null;

    public TutorialActionSelector() {
        super(null);
        isVisible = this::isEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TutorialActionSelector setPos(int x, int y) {
        renderPos = TutorialMapElement.getRenderPos(x, y);
        return this;
    }

    public TutorialActionSelector setUnit(UnitLike unit) {
        this.unit = unit;
        renderPos = unit.getRenderPos();
        setEnabled(true);
        clear();
        for (Action action : unit.data.type.actions) {
            addActionEnabled(action, null);
        }
        return this;
    }

    public TutorialActionSelector setActionState(Action a, ActionIconType type) {
        if (actionMap.containsKey(a))
            actionMap.get(a).type = type;
        return this;
    }

    public void reset() {
        setEnabled(false);
        clear();
    }

    @Override
    protected ObjPos getRenderPos() {
        return renderPos;
    }

    @Override
    protected Optional<Integer> getPerTurnActionCost(Action a) {
        return unit.data.type.getPerTurnActionCost(a);
    }

    @Override
    protected Optional<Integer> getActionCost(Action a) {
        return unit.data.type.getActionCost(a);
    }

    @Override
    protected boolean removeActionEnergyCost(Action a) {
        return a == Action.STEALTH && unit.data.stealthMode ||
                a == Action.MINE && unit.data.mining;
    }

    @Override
    protected void setCost(EnergyCostDisplay display, Action a, int cost) {
        display.setCost(cost, true);
    }
}
