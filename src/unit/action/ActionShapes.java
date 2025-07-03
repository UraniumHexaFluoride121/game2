package unit.action;

import render.Renderable;
import render.types.text.TextRenderable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public abstract class ActionShapes {
    public static final Path2D.Float MOVE = new Path2D.Float();
    public static final Shape FLAG;
    public static final Shape SHIELD, SHIELD_REGEN;
    public static final Path2D.Float SHIP = new Path2D.Float();
    public static final Shape EYE;
    public static final Shape SUPPLY;
    public static Path2D.Float ENERGY;
    public static final Shape REPAIR;
    public static final Shape STEALTH;

    static {
        MOVE.moveTo(.37f, .37f);
        MOVE.lineTo(.75f, .75f);
        MOVE.closePath();

        MOVE.moveTo(.25f, .25f);
        MOVE.lineTo(.2501f, .2501f);
        MOVE.closePath();

        MOVE.moveTo(.45f, .75f);
        MOVE.lineTo(.75f, .75f);
        MOVE.closePath();

        MOVE.moveTo(.75f, .45f);
        MOVE.lineTo(.75f, .75f);
        MOVE.closePath();
    }

    static {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(.4, .2);
        path.lineTo(.2, .75);
        path.closePath();

        path.moveTo(.32, .7);
        path.quadTo(
                .6, .6,
                .75, .7
        );
        path.lineTo(.8, .5);
        path.quadTo(
                .6, .4,
                .4, .5
        );
        path.closePath();
        FLAG = Renderable.add(Renderable.outlineShape(path, 0.075f), path);
    }

    static {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(0.5f, 0);
        path.curveTo(1, 0, 1, 0.85f, 1, 0.9f);
        path.curveTo(0.8f, 0.8f, 0.6f, 0.83f, 0.5f, 0.95f);
        path.curveTo(0.4f, 0.83f, 0.2f, 0.8f, 0, 0.9f);
        path.curveTo(0, 0.85f, 0, 0, 0.5f, 0);
        path.closePath();
        AffineTransform t = new AffineTransform();
        t.translate(0.5f, 0.5f);
        t.scale(0.5f, 0.5f);
        t.translate(-0.5f, -0.5f);
        SHIELD = t.createTransformedShape(Renderable.add(Renderable.outlineShape(path, 0.15f), path));
        AffineTransform t2 = new AffineTransform();
        t2.translate(0.5f, 0.5f);
        SHIELD_REGEN = Renderable.subtract(SHIELD, t2.createTransformedShape(TextRenderable.plus(0.5f)));
    }

    static {
        AffineTransform t = new AffineTransform();
        t.translate(0.5f, 0.5f);
        t.scale(0.9f, 0.9f);
        REPAIR = t.createTransformedShape(TextRenderable.repairShape(0.12f));
    }

    static {
        SHIP.moveTo(.3, .53);
        SHIP.lineTo(.35, .57);
        SHIP.lineTo(.45, .6);
        SHIP.lineTo(.6, .58);
        SHIP.lineTo(.7, .52);
        SHIP.lineTo(.7, .48);
        SHIP.lineTo(.6, .42);
        SHIP.lineTo(.45, .4);
        SHIP.lineTo(.35, .43);
        SHIP.lineTo(.3, .47);
        SHIP.closePath();
        SHIP.moveTo(.4, .65);
        SHIP.lineTo(.45, .65);
        SHIP.lineTo(.45, .35);
        SHIP.lineTo(.4, .35);
        SHIP.closePath();
        SHIP.moveTo(.38, .64);
        SHIP.lineTo(.6, .64);
        SHIP.lineTo(.6, .63);
        SHIP.lineTo(.38, .63);
        SHIP.closePath();
        SHIP.moveTo(.38, .36);
        SHIP.lineTo(.6, .36);
        SHIP.lineTo(.6, .37);
        SHIP.lineTo(.38, .37);
        SHIP.closePath();
    }

    static {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(0, 0.5f);
        path.curveTo(0.21f, 0.83f, 0.79f, 0.83f, 1, 0.5f);
        path.curveTo(0.79f, 0.17f, 0.21f, 0.17f, 0, 0.5f);
        path.closePath();
        path.setWindingRule(Path2D.WIND_EVEN_ODD);
        path.append(new Ellipse2D.Float(0.3f, 0.3f, 0.4f, 0.4f), false);
        AffineTransform t = new AffineTransform();
        t.translate(.5, .5);
        t.scale(0.65f, 0.65f);
        t.translate(-.5, -.5);
        EYE = t.createTransformedShape(path);
    }

    static {
        AffineTransform t = new AffineTransform();
        t.translate(0.5f, 0.5f);
        t.scale(0.9f, 0.9f);
        SUPPLY = t.createTransformedShape(TextRenderable.resupplyShape(0.12f));
    }

    static {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(0.35f, 0.22f);
        path.lineTo(0.35f, 0.3f);
        path.lineTo(1 - 0.35f, 0.3f);
        path.lineTo(1 - 0.35f, 0.22f);
        path.closePath();
        path.moveTo(0.35f, 1 - 0.22f);
        path.lineTo(0.35f, 1 - 0.3f);
        path.lineTo(1 - 0.35f, 1 - 0.3f);
        path.lineTo(1 - 0.35f, 1 - 0.22f);
        path.closePath();
        path.moveTo(0.4f, 0.33f);
        path.lineTo(0.4f, 1 - 0.33f);
        path.lineTo(1 - 0.4f, 1 - 0.33f);
        path.lineTo(1 - 0.4f, 0.33f);
        path.closePath();
        AffineTransform t = new AffineTransform();
        t.rotate(Math.toRadians(30), 0.5f, 0.5f);
        t.translate(0.5f, 0.5f);
        t.scale(1.1f, 1.1f);
        t.translate(-0.5f, -0.5f);
        ENERGY = (Path2D.Float) path.createTransformedShape(t);
    }

    static {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(.27f, .27f);
        path.lineTo(.73f, .73f);
        Shape line = Renderable.outlineShape(path, 0.06f);
        STEALTH = Renderable.add(Renderable.subtract(Renderable.add(EYE, Renderable.outlineShape(EYE, 0.02f)), Renderable.outlineShape(path, 0.11f)), line);
    }
}
