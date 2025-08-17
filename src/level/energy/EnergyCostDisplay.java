package level.energy;

import level.Level;
import render.types.text.TextRenderable;

import static render.types.text.StyleElement.*;

public class EnergyCostDisplay extends EnergyDisplay {
    private final boolean perTurn;
    private int cost = 0;

    public EnergyCostDisplay(boolean perTurn) {
        super(10, true);
        this.perTurn = perTurn;
    }

    public void renderToEnergyManager(Level level) {
        if (perTurn)
            level.levelRenderer.energyManager.updateIncomeChange(cost);
        else
            level.levelRenderer.energyManager.updateAvailableChange(cost);
    }

    public void setCost(int cost, Level level) {
        setCost(cost, level.levelRenderer.energyManager.canAfford(level.getThisTeam(), -cost, false));
    }

    public void setCost(int cost, boolean canAfford) {
        this.cost = cost;
        if (perTurn) {
            setText((cost < 0 ? NO_COLOUR : ENERGY_COST_GREEN).display + EnergyManager.numberText(cost) + TextRenderable.ENERGY.display + "/ Turn");
        } else {
            setText((cost < 0 ? (canAfford ? NO_COLOUR : ENERGY_COST_RED) : ENERGY_COST_GREEN).display + EnergyManager.numberText(cost) + TextRenderable.ENERGY.display);
        }
    }
}
