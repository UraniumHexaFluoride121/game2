package level.tutorial.sequence.action;

import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;
import level.tutorial.TutorialManager;
import render.RenderOrder;
import render.Renderable;
import render.level.tile.RenderElement;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Function;

public class TutorialUI implements TutorialSequenceElement {
    private Function<Renderable, RenderElement> createRenderElement;
    private Renderable renderable = g -> {
    };
    private final String name;
    private final boolean map;

    public static TutorialUI onMap(Level l, String name) {
        return new TutorialUI(r -> new RenderElement(l.levelRenderer.mainRenderer, RenderOrder.TUTORIAL_UI, r), name, true);
    }

    public static TutorialUI onUI(Level l, String name) {
        return new TutorialUI(r -> new RenderElement(l.levelRenderer.levelUIRenderer, RenderOrder.TUTORIAL_LEVEL_UI, r), name, false);
    }

    public static TutorialAction remove(String name) {
        return new TutorialAction(() -> TutorialManager.renderElements.remove(name).delete());
    }

    private TutorialUI(Function<Renderable, RenderElement> createRenderElement, String name, boolean map) {
        this.createRenderElement = createRenderElement;
        this.name = name;
        this.map = map;
    }

    public TutorialUI rectangle(float x, float y, float width, float height, Color color, StrokeWidth strokeWidth) {
        ObjPos pos = map ? Tile.getFractionalRenderPos(x, y).subtract(width / 2, 0) : new ObjPos(x - width / 2, y);
        BasicStroke stroke = Renderable.roundedStroke(strokeWidth.width);
        RoundRectangle2D.Float rectangle = new RoundRectangle2D.Float(pos.x, pos.y, width, height, 0.3f, 0.3f);
        renderable = renderable.andThen(g -> {
            g.setColor(color);
            g.setStroke(stroke);
            g.draw(rectangle);
        });
        return this;
    }

    @Override
    public void start() {
        RenderElement e = createRenderElement.apply(renderable);
        TutorialManager.renderElements.put(name, e);
        TutorialSequenceElement.next();
    }

    @Override
    public void end() {

    }

    @Override
    public void delete() {
        renderable = null;
        createRenderElement = null;
    }

    public enum StrokeWidth {
        NARROW(0.15f), NORMAL(0.3f);

        public final float width;

        StrokeWidth(float width) {
            this.width = width;
        }
    }
}
