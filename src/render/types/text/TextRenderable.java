package render.types.text;

import foundation.math.ObjPos;
import render.Renderable;
import render.level.tile.HexagonRenderer;
import render.texture.ImageRenderer;
import render.texture.ResourceLocation;
import render.types.box.UIBox;
import render.types.input.button.UIShapeButton;
import unit.action.ActionShapes;

import java.awt.*;
import java.awt.geom.*;
import java.util.function.Function;

public enum TextRenderable {
    ENERGY(image("icons/energy.png", 26, 37)),
    STAR(fillShape(starShape(5, 0.15f, 0.35f), true, false, 20, 25)),

    DAMAGE_ICON(drawShape(damageShape(), Renderable.sharpCornerStroke(2f), true, false, 25, 28)),
    SHIELD_ICON(fillShape(shieldShape(), true, false, 25, 28)),
    SHIELD_REGEN_ICON(fillShape(shieldRegenShape(), true, false, 25, 28)),
    MOVE_ICON(drawShape(moveShape(), Renderable.roundedStroke(3f), true, false, 25, 28)),
    RANGE_ICON(fillShape(rangeShape(), true, false, 35, 28)),
    AMMO_ICON(fillShape(ammoShape(), true, false, 25, 28)),
    HP_ICON(drawShape(hitPointShape(), Renderable.roundedStroke(2.5f), true, false, 25, 28)),
    REPAIR_ICON(fillShape(repairShape(0.15f), true, false, 25, 28)),
    RESUPPLY_ICON(fillShape(resupplyShape(0.15f), true, false, 25, 28)),
    STEALTH_ICON(fillShape(stealthShape(), true, false, 25, 36)),
    ENERGY_ICON(fillShape(antimatterShape(), true, false, 25, 28)),
    CAPTURE_ICON(fillShape(captureShape(), true, false, 25, 28)),
    VIEW_RANGE_ICON(drawShape(viewRangeShape(), Renderable.roundedStroke(3.5f), true, false, 25, 28)),

    FIGHTER_ICON(fillShape(fighterIcon(), true, false, 30, 18)),
    CORVETTE_ICON(fillShape(corvetteIcon(), true, false, 30, 18)),
    CRUISER_ICON(fillShape(cruiserIcon(), true, false, 30, 18)),
    CAPITAL_SHIP_ICON(fillShape(capitalShipIcon(), true, false, 30, 18)),

    RIGHT_ARROW(drawShape(arrowShape(-0.5f, 0, 0.5f, 0, 0.4f, 50), Renderable.roundedStroke(3), true, false, 20, 20));

    public final Function<TextStyle, TextRenderElement> renderable;
    public final String display;

    TextRenderable(Function<TextStyle, TextRenderElement> renderable) {
        this.renderable = renderable;
        display = "{" + name() + "}";
    }

    public static TextRenderElement get(String name, TextStyle style) {
        for (TextRenderable value : values()) {
            if (name.equals(value.name()))
                return value.renderable.apply(style);
        }
        throw new IllegalArgumentException("Text renderable \"" + name + "\" is not valid");
    }

    private static Function<TextStyle, TextRenderElement> image(String path, float advance, float size) {
        return s -> new TextRenderElement(advance, size, s, g -> {
            g.translate(0, -size / 2 + 7);
            ImageRenderer.renderImage(new ResourceLocation(path), true, true).render(g, size);
            g.translate(0, size / 2 - 7);
        });
    }

    private static Function<TextStyle, TextRenderElement> fillShape(Shape shape, boolean center, boolean textInline, float advance, float size) {
        AffineTransform t = new AffineTransform();
        t.scale(size, size);
        Shape renderShape = t.createTransformedShape(shape);
        float offset = textInline ? 5 : 7;
        return s -> new TextRenderElement(advance, size, s, g -> {
            g.setColor(s.colour);
            if (center)
                g.translate(size / 2, offset);
            else
                g.translate(0, -size / 2 + offset);
            g.fill(renderShape);
            if (center)
                g.translate(-size / 2, -offset);
            else
                g.translate(0, size / 2 - offset);
        });
    }

    private static Function<TextStyle, TextRenderElement> drawShape(Shape shape, Stroke stroke, boolean center, boolean textInline, float advance, float size) {
        AffineTransform t = new AffineTransform();
        t.scale(size, size);
        Shape renderShape = t.createTransformedShape(shape);
        float offset = textInline ? 5 : 7;
        return s -> new TextRenderElement(advance, size, s, g -> {
            g.setColor(s.colour);
            g.setStroke(stroke);
            if (center)
                g.translate(size / 2, offset);
            else
                g.translate(0, -size / 2 + offset);
            g.draw(renderShape);
            if (center)
                g.translate(-size / 2, -offset);
            else
                g.translate(0, size / 2 - offset);
        });
    }

    private static Path2D.Float starShape(int count, float inner, float outer) {
        Path2D.Float path = new Path2D.Float();
        for (int i = 0; i < count; i++) {
            double outerAngle = Math.toRadians(90 + 360 * ((float) i / count));
            double innerAngle = Math.toRadians(90 + 360 * ((i + 0.5f) / count));
            if (i == 0)
                path.moveTo(outer * (float) Math.cos(outerAngle), outer * (float) Math.sin(outerAngle));
            else
                path.lineTo(outer * (float) Math.cos(outerAngle), outer * (float) Math.sin(outerAngle));
            path.lineTo(inner * (float) Math.cos(innerAngle), inner * (float) Math.sin(innerAngle));
        }
        path.closePath();
        return path;
    }

    private static Shape damageShape() {
        AffineTransform t = new AffineTransform();
        t.translate(-0.5f, -0.5f);
        t.scale(1f / Renderable.SCALING, 1f / Renderable.SCALING);
        return t.createTransformedShape(UIShapeButton.target(new UIBox(1, 1)));
    }

    private static Shape moveShape() {
        AffineTransform t = new AffineTransform();
        t.translate(-0.5f, -0.5f);
        return t.createTransformedShape(ActionShapes.MOVE);
    }

    private static Shape shieldShape() {
        AffineTransform t = new AffineTransform();
        t.translate(-0.5f, -0.5f);
        return t.createTransformedShape(ActionShapes.SHIELD);
    }

    private static Shape shieldRegenShape() {
        AffineTransform t = new AffineTransform();
        t.translate(-0.5f, -0.5f);
        return t.createTransformedShape(ActionShapes.SHIELD_REGEN);
    }

    private static Shape stealthShape() {
        AffineTransform t = new AffineTransform();
        t.translate(-0.5f, -0.5f);
        return t.createTransformedShape(ActionShapes.STEALTH);
    }

    private static Shape antimatterShape() {
        AffineTransform t = new AffineTransform();
        t.translate(-0.5f, -0.5f);
        return t.createTransformedShape(ActionShapes.ENERGY);
    }

    private static Shape captureShape() {
        AffineTransform t = new AffineTransform();
        t.translate(-0.5f, -0.5f);
        return t.createTransformedShape(ActionShapes.FLAG);
    }

    public static Shape resupplyShape(float outline) {
        return doubleShapeUpper(ammoShape(), plus(1), outline);
    }

    private static Shape rangeShape() {
        Path2D.Float path = arrowShape(-0.4f, 0, 0.1f, 0, 0.2f, 50);
        AffineTransform t = new AffineTransform();
        t.translate(-0.3f, -0.5f);
        t.scale(1f / Renderable.SCALING, 1f / Renderable.SCALING);
        Shape target = t.createTransformedShape(UIShapeButton.target(new UIBox(1, 1)));
        return Renderable.add(Renderable.subtract(Renderable.outlineShape(target, 0.07f), Renderable.outlineShape(path, 0.18f)), Renderable.outlineShape(path, 0.07f));
    }

    public static Path2D.Float ammoShape() {
        Path2D.Float path = new Path2D.Float();
        path.append(new Rectangle2D.Float(-0.32f, -0.2f, 0.15f, 0.3f), false);
        path.append(new Rectangle2D.Float(-0.32f, -0.3f, 0.15f, 0.05f), false);
        path.append(new Ellipse2D.Float(-0.32f, 0.1f - 0.075f, 0.15f, 0.15f), false);

        path.append(new Rectangle2D.Float(-0.075f, -0.05f, 0.15f, 0.3f), false);
        path.append(new Rectangle2D.Float(-0.075f, -0.15f, 0.15f, 0.05f), false);
        path.append(new Ellipse2D.Float(-0.075f, 0.25f - 0.075f, 0.15f, 0.15f), false);

        path.append(new Rectangle2D.Float(0.18f, -0.2f, 0.15f, 0.3f), false);
        path.append(new Rectangle2D.Float(0.18f, -0.3f, 0.15f, 0.05f), false);
        path.append(new Ellipse2D.Float(0.18f, 0.1f - 0.075f, 0.15f, 0.15f), false);
        return path;
    }

    private static Path2D.Float hitPointShape() {
        float size = 0.3f;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(Math.cos(Math.toRadians(30 + 60 * 0)) * size, Math.sin(Math.toRadians(30 + 60 * 0)) * size);
        path.lineTo(Math.cos(Math.toRadians(30 + 60 * 1)) * size, Math.sin(Math.toRadians(30 + 60 * 1)) * size);
        path.lineTo(Math.cos(Math.toRadians(30 + 60 * 2)) * size, Math.sin(Math.toRadians(30 + 60 * 2)) * size);
        path.lineTo(Math.cos(Math.toRadians(30 + 60 * 3)) * size, Math.sin(Math.toRadians(30 + 60 * 3)) * size);
        path.lineTo(Math.cos(Math.toRadians(30 + 60 * 4)) * size, Math.sin(Math.toRadians(30 + 60 * 4)) * size);
        path.lineTo(Math.cos(Math.toRadians(30 + 60 * 5)) * size, Math.sin(Math.toRadians(30 + 60 * 5)) * size);
        path.closePath();
        path.moveTo(Math.cos(Math.toRadians(30 + 60 * 0)) * size, Math.sin(Math.toRadians(30 + 60 * 0)) * size);
        path.lineTo(0, 0);
        path.closePath();
        path.moveTo(Math.cos(Math.toRadians(30 + 60 * 2)) * size, Math.sin(Math.toRadians(30 + 60 * 2)) * size);
        path.lineTo(0, 0);
        path.closePath();
        path.moveTo(Math.cos(Math.toRadians(30 + 60 * 4)) * size, Math.sin(Math.toRadians(30 + 60 * 4)) * size);
        path.lineTo(0, 0);
        path.closePath();
        return path;
    }

    private static Shape viewRangeShape() {
        float size = 0.25f;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-size, -size);
        path.lineTo(-size + 0.01f, -size + 0.01f);
        path.closePath();
        path.append(new Arc2D.Float(-size * 2 - size, -size * 2 - size, size * 4, size * 4, -90, 90, Arc2D.OPEN), false);
        path.append(new Arc2D.Float(-size - size, -size - size, size * 2, size * 2, -90, 90, Arc2D.OPEN), false);
        return path;
    }

    public static Shape repairShape(float outline) {
        return doubleShapeUpper(Renderable.outlineShape(hitPointShape(), 0.1f), plus(1), outline);
    }

    public static Shape plus(float scale) {
        AffineTransform t2 = new AffineTransform();
        t2.translate(-0.5f, -0.5f);
        t2.scale(1f / Renderable.SCALING, 1f / Renderable.SCALING);
        return t2.createTransformedShape(UIShapeButton.plus(new UIBox(1, 1), 0.6f * scale, 0.3f));
    }

    public static Shape doubleShapeLower(Shape a, Shape b, float outline) {
        AffineTransform t = new AffineTransform();
        t.scale(0.9f, 0.9f);
        t.translate(-0.05f, 0.05f);

        AffineTransform t2 = new AffineTransform();
        t2.translate(0.25f, -0.15f);
        t2.scale(0.55f, 0.55f);
        Shape b2 = t2.createTransformedShape(b);
        return Renderable.add(Renderable.subtract(t.createTransformedShape(a), Renderable.outlineShape(b2, outline)), b2);
    }

    public static Shape doubleShapeUpper(Shape a, Shape b, float outline) {
        AffineTransform t = new AffineTransform();
        t.scale(0.9f, 0.9f);
        t.translate(-0.05f, -0.05f);

        AffineTransform t2 = new AffineTransform();
        t2.translate(0.25f, 0.15f);
        t2.scale(0.55f, 0.55f);
        Shape b2 = t2.createTransformedShape(b);
        return Renderable.add(Renderable.subtract(t.createTransformedShape(a), Renderable.outlineShape(b2, outline)), b2);
    }

    public static Path2D.Float arrowShape(float x1, float y1, float x2, float y2, float headLength, float degrees) {
        Path2D.Float path = new Path2D.Float();
        ObjPos tip = new ObjPos(x2, y2);
        ObjPos vector = new ObjPos(x1, y1).subtract(tip).setLength(headLength);
        ObjPos p1 = tip.copy().add(vector.copy().rotate((float) Math.toRadians(degrees)));
        ObjPos p2 = tip.copy().add(vector.copy().rotate(-(float) Math.toRadians(degrees)));

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.closePath();

        path.moveTo(x2, y2);
        path.lineTo(p1.x, p1.y);
        path.closePath();

        path.moveTo(x2, y2);
        path.lineTo(p2.x, p2.y);
        path.closePath();
        return path;
    }

    private static Path2D.Float fighterIcon() {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-0.5f, 0.4f);
        path.lineTo(0.5f, 0);
        path.lineTo(-0.5f, -0.4f);
        path.closePath();
        return path;
    }

    private static Path2D.Float corvetteIcon() {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-0.5f, 0);
        path.lineTo(0, 0.5f);
        path.lineTo(0.5f, 0);
        path.lineTo(0, -0.5f);
        path.closePath();
        return path;
    }

    private static Path2D.Float cruiserIcon() {
        return HexagonRenderer.hexagonShape(1, 0, true);
    }

    private static Path2D.Float capitalShipIcon() {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-0.5f, -0.5f);
        path.lineTo(0.5f, -0.5f);
        path.lineTo(0.5f, -0.3f);
        path.lineTo(-0.5f, -0.3f);
        path.closePath();
        path.moveTo(-0.5f, 0);
        path.lineTo(-0.5f, -0.15f);
        path.lineTo(0.5f, -0.15f);
        path.lineTo(0.5f, 0);
        path.lineTo(0, 0.5f);
        path.closePath();
        return path;
    }
}
