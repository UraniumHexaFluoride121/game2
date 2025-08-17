package render.types.box.display.tutorial;

import foundation.Deletable;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;
import render.types.text.TextRenderer;

import java.awt.*;
import java.util.function.Consumer;

public class TutorialMapText implements Renderable, Deletable {
    private final TextRenderer text;
    private Consumer<TutorialMapText> onReset;
    private Consumer<TutorialMapText> onRender;
    private TutorialMapElement map;
    private boolean visible = true;
    private final float x, y;

    public TutorialMapText(TutorialMapElement map, float x, float y, TextRenderer text, Consumer<TutorialMapText> onReset, Consumer<TutorialMapText> onRender) {
        this.text = text;
        this.onReset = onReset == null ? TutorialMapText::doNothing : onReset;
        this.onRender = onRender == null ? TutorialMapText::doNothing : onRender;
        this.x = x;
        this.y = y;
        this.map = map;
    }

    public TutorialMapText(TutorialMapElement map, float x, float y, String text, HorizontalAlign align, Consumer<TutorialMapText> onReset, Consumer<TutorialMapText> onRender) {
        this(map, x, y, new TextRenderer(text, 0.7f).setTextAlign(align).setBold(true), onReset, onRender);
    }

    public TextRenderer getText() {
        return text;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void updateText(String s) {
        text.updateText(s);
    }

    public void reset() {
        onReset.accept(this);
    }

    @Override
    public void delete() {
        onRender = null;
        onReset = null;
        map = null;
    }

    @Override
    public void render(Graphics2D g) {
        onRender.accept(this);
        if (visible) {
            GameRenderer.renderOffset(x, y, g, () -> {
                text.render(g);
            });
        }
    }

    public void task(float time, Consumer<TutorialMapText> action) {
        if (time == 0)
            action.accept(this);
        map.addSingleTask(time, () -> action.accept(this));
    }

    public static void hide(TutorialMapText text) {
        text.visible = false;
    }

    public static void show(TutorialMapText text) {
        text.visible = true;
    }

    public static Consumer<TutorialMapText> setText(String s) {
        return t -> t.text.updateText(s);
    }

    public static void doNothing(TutorialMapText text) {

    }
}
