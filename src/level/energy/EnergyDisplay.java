package level.energy;

import render.GameRenderer;
import render.Renderable;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.text.TextRenderer;
import render.types.text.TextAlign;

import java.awt.*;

import static render.types.text.UITextLabel.*;

public class EnergyDisplay implements Renderable {
    private final UIBox energyBox;
    public final TextRenderer text = new TextRenderer(null, .6f, TEXT_COLOUR)
            .setBold(true).setTextAlign(TextAlign.CENTER);
    private final float width;

    public EnergyDisplay(float width) {
        this.width = width;
        energyBox = new UIBox(width, 1).setCorner(.25f).setColourTheme(UIColourTheme.DARK_GRAY);
    }

    public EnergyDisplay setText(String s) {
        text.updateText(s);
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            g.translate(-width / 2, 0);
            energyBox.render(g);
            GameRenderer.renderTransformed(g, () -> {
                g.translate((width - .5f) / 2, .5f - .23f);
                text.render(g);
            });
            GameRenderer.renderTransformed(g, () -> {
                g.translate(width - 0.5f, 0.5f);
                EnergyManager.ENERGY_IMAGE.render(g, 1);
            });
        });
    }
}
