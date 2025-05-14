package level.energy;

import level.Level;
import render.GameRenderer;
import render.Renderable;
import render.types.text.TextRenderer;
import render.types.text.TextAlign;
import render.UIColourTheme;
import render.types.box.UIBox;

import java.awt.*;

import static render.types.text.UITextLabel.*;

public class EnergyCostDisplay implements Renderable {
    private final UIBox energyBox;
    private final boolean perTurn;
    private final TextRenderer energyText = new TextRenderer(null, .6f, RED_TEXT_COLOUR)
            .setBold(true).setTextAlign(TextAlign.CENTER);
    private final TextRenderer perTurnText = new TextRenderer(null, .6f, RED_TEXT_COLOUR)
            .setBold(true).setTextAlign(TextAlign.RIGHT);
    private int cost = 0;

    public EnergyCostDisplay(boolean perTurn) {
        this.perTurn = perTurn;
        energyBox = new UIBox(perTurn ? 4 : 2, 1).setCorner(.25f).setColourTheme(UIColourTheme.DARK_GRAY);
        perTurnText.updateText("/ Turn");
    }

    @Override
    public void render(Graphics2D g) {
        if (perTurn) {
            g.translate(-2f, 0);
            energyBox.render(g);
            g.translate(3.6f, .5f - .23f);
            GameRenderer.renderTransformed(g, () -> {
                perTurnText.render(g);
            });
            g.translate(-2, .23f);
            EnergyManager.ENERGY_IMAGE.render(g, 1);
            g.translate(-1.6 / 2f, -.23f);
            GameRenderer.renderTransformed(g, () -> {
                energyText.render(g);
            });
        } else {
            g.translate(-1f, 0);
            energyBox.render(g);
            g.translate(1.5f, .5f);
            EnergyManager.ENERGY_IMAGE.render(g, 1);
            g.translate(-.75f, -.23f);
            GameRenderer.renderTransformed(g, () -> {
                energyText.render(g);
            });
        }
    }

    public void renderToEnergyManager(Level level) {
        if (perTurn)
            level.levelRenderer.energyManager.updateIncomeChange(cost);
        else
            level.levelRenderer.energyManager.updateAvailableChange(cost);
    }

    public void setCost(int cost, Level level) {
        this.cost = cost;
        if (perTurn) {
            energyText.updateText(EnergyManager.numberText(cost));
            energyText.setTextColour(cost < 0 ? TEXT_COLOUR : GREEN_TEXT_COLOUR);
            perTurnText.setTextColour(cost < 0 ? TEXT_COLOUR : GREEN_TEXT_COLOUR);
        } else {
            energyText.updateText(EnergyManager.numberText(cost));
            energyText.setTextColour(cost < 0 ? (level.levelRenderer.energyManager.canAfford(level.getThisTeam(), -cost, false) ? TEXT_COLOUR : RED_TEXT_COLOUR) : GREEN_TEXT_COLOUR);
        }
    }
}
