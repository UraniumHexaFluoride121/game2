package render.level.ui;

import render.GameRenderer;
import render.anim.PowAnimation;
import render.types.text.TextRenderer;
import render.types.text.TextAlign;

import java.awt.*;

import static unit.Unit.*;

public class UnitTextUI {
    public static final Color RESUPPLY_COLOR = new Color(174, 100, 46);
    private final TextRenderer text;
    private final PowAnimation anim = new PowAnimation(1.2f, .7f);
    private final float x, y, moveFactor;

    public UnitTextUI(String s, float x, float y, float textSize, float moveFactor, Color textColor) {
        text = new TextRenderer(null, textSize, textColor)
                .setBold(true).setTextAlign(TextAlign.CENTER).setRenderBorder(0.15f, 0.3f, HP_BACKGROUND_COLOUR);
        this.x = x;
        this.y = y;
        this.moveFactor = moveFactor;
        text.updateText(s);
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
