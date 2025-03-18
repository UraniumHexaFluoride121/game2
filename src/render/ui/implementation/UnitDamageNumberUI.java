package render.ui.implementation;

import render.GameRenderer;
import render.anim.PowAnimation;
import render.renderables.text.FixedTextRenderer;
import render.renderables.text.TextAlign;

import java.awt.*;

import static level.energy.EnergyManager.*;

public class UnitDamageNumberUI {
    private final FixedTextRenderer text;
    private final PowAnimation anim = new PowAnimation(1, .7f);
    private final float x, y, moveFactor;

    public UnitDamageNumberUI(float damage, float x, float y, float moveFactor) {
        text = new FixedTextRenderer(null, .7f, numberColour(damage))
                .setBold(true).setTextAlign(TextAlign.CENTER);
        this.x = x;
        this.y = y;
        this.moveFactor = moveFactor;
        text.updateText(numberText(damage, 1));
    }

    public boolean render(Graphics2D g) {
        if (anim.finished())
            return true;
        GameRenderer.renderOffset(x, y + anim.normalisedProgress() * moveFactor, g, () -> {
            text.render(g);
        });
        return false;
    }
}
