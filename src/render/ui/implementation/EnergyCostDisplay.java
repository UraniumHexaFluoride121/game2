package render.ui.implementation;

import level.Level;
import level.energy.EnergyManager;
import render.GameRenderer;
import render.Renderable;
import render.renderables.text.FixedTextRenderer;
import render.renderables.text.TextAlign;
import render.ui.UIColourTheme;
import render.ui.types.UIBox;

import java.awt.*;

import static render.ui.types.UITextLabel.*;

public class EnergyCostDisplay implements Renderable {
    private final UIBox energyBox = new UIBox(2, 1).setCorner(.25f).setColourTheme(UIColourTheme.DARK_GRAY);
    private final FixedTextRenderer energyText = new FixedTextRenderer(null, .6f, RED_TEXT_COLOUR)
            .setBold(true).setTextAlign(TextAlign.CENTER);
    private int cost = 0;

    @Override
    public void render(Graphics2D g) {
        g.translate(-1f, 0);
        energyBox.render(g);
        g.translate(1.5f, .5f);
        EnergyManager.ENERGY_IMAGE.render(g, 1);
        g.translate(-.75f, -.23f);
        GameRenderer.renderTransformed(g, () -> {
            energyText.render(g);
        });
    }

    public void renderToEnergyManager(Level level) {
        level.levelRenderer.energyManager.updateAvailableChange(cost);
    }

    public void setCost(int cost, Level level) {
        this.cost = cost;
        energyText.updateText(String.valueOf(cost));
        energyText.setTextColour(cost < 0 ? (level.levelRenderer.energyManager.canAfford(level.getThisTeam(), -cost, false) ? TEXT_COLOUR : RED_TEXT_COLOUR) : GREEN_TEXT_COLOUR);
    }
}
